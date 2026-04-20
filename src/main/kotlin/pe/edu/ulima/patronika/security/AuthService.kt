package pe.edu.ulima.patronika.security

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import pe.edu.ulima.patronika.dto.*
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.model.User
import pe.edu.ulima.patronika.repository.UserRepository
import pe.edu.ulima.patronika.services.UsersService
import java.time.OffsetDateTime
import java.util.*

@Service
class AuthService(
    private val usersService: UsersService,
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    @param:Qualifier("webClient") private val webClient: WebClient,
    @param:Qualifier("xmlMapper") private val xmlMapper: XmlMapper
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    private suspend fun authenticateWS(
        userCode: String,
        password: String,
        path: String
    ): LoginResult {
        // 1. Construir objeto de petición
        val requestEnvelope = LoginEnvelopeRequest(
            body = LoginBodyRequest(
                login = LoginRequestWS(
                    userCode = userCode,
                    password = password
                )
            )
        )

        // 2. Convertir a XML
        val requestXml = xmlMapper.writeValueAsString(requestEnvelope)

        log.debug("SOAP request XML -> {}: {}", path, requestXml)

        // 3. Enviar petición y recibir respuesta como String
        val responseXml: String = webClient.post()
            .uri(path)
            .contentType(MediaType.parseMediaType("text/xml; charset=UTF-8"))
            .accept(MediaType.TEXT_XML, MediaType.APPLICATION_XML)
            .bodyValue(requestXml)
            .exchangeToMono { response ->
                response.bodyToMono<String>()
                    .defaultIfEmpty("")
                    .map { body ->
                        val status = response.statusCode().value()

                        if (status == 401 || status == 403) {
                            throw UnauthorizedException(
                                "No autorizado por el servicio (HTTP $status). Verifica WS_USERNAME/WS_PASSWORD y que WS_URL sea la URL final (https)."
                            )
                        }

                        if (response.statusCode().isError) {
                            throw RuntimeException(
                                "Error HTTP $status del servicio. Body: ${body.take(500)}"
                            )
                        }

                        if (body.isBlank()) {
                            throw RuntimeException(
                                "Respuesta vacía del servicio (HTTP $status)"
                            )
                        }

                        body
                    }
            }
            .awaitSingle()

        log.debug("SOAP response XML <- {}: {}", path, responseXml)

        // 4. Parsear respuesta XML a objeto
        val responseEnvelope = xmlMapper.readValue(responseXml, LoginEnvelopeResponse::class.java)

        // 5. Extraer resultado
        val result = responseEnvelope.body?.loginResponse?.loginResult
            ?: throw RuntimeException("Respuesta del servicio inválida")

        // 6. Verificar si hay error
        if (!result.txMsgError.isNullOrBlank() && result.txMsgError != "TOKEN GENERADO") {
            throw UnauthorizedException(result.txMsgError)
        }

        return result
    }

    suspend fun login(req: LoginRequest): User {
        val wsResult = authenticateWS(
            userCode = req.userCode,
            password = req.password,
            path = "/WsLoginULimaMovil"
        )

        val clearRpta = wsResult.txRpta ?: ""
        val clearToken = wsResult.txTknSesn ?: ""

        val userToSave = withContext(Dispatchers.IO) {
            userRepository.findByUserCode(wsResult.coUser ?: req.userCode)?.apply {
                loggedIn = true
                rpta = hashEncoder.encode(clearRpta)
                token = hashEncoder.encode(clearToken)
                updatedAt = OffsetDateTime.now()
            } ?: User(
                fullName = wsResult.txAlumno ?: "",
                userCode = wsResult.coUser ?: "",
                loggedIn = true,
                userType = 0,
                rpta = hashEncoder.encode(clearRpta),
                token = hashEncoder.encode(clearToken),
                createdAt = OffsetDateTime.now()
            )
        }

        val savedUser = withContext(Dispatchers.IO) {
            userRepository.save(userToSave)
        }

        savedUser.rpta = clearRpta
        savedUser.token = clearToken
        return savedUser
    }

//    private fun generateUniqueToken(): String {
//        var token: Int
//        do {
//            token = Random.nextInt(1000, 10000)
//        } while (userRepository.findAll().any { it.token == token.toString() })
//        return token.toString()
//    }
//
//    fun loginOld(req: LoginRequest): Map<String, String> {
//        val user = userRepository.findByUserCode(req.userCode)
//            ?: throw UnauthorizedException("Usuario no registrado")
//        val ok = hashEncoder.matches(req.password, user.password)
//        if (!ok) throw UnauthorizedException("Credenciales incorrectas")
//
//        val newToken = if (user.token == "10000") "10000" else generateUniqueToken()
//
//        user.token = newToken
//        user.loggedIn = true
//        user.updatedAt = OffsetDateTime.now()
//
//        userRepository.save(user)
//
//        val dto = mapOf(
//            "userId" to user.id.toString(),
//            "fullName" to user.fullName,
//            "userCode" to user.userCode,
//            "lastName1" to "",
//            "lastName2" to "",
//            "names" to user.fullName,
//            "specialtyName" to "",
//            "urlPhoto" to "",
//            "token" to user.token!!
//        )
//
//        return dto
//    }

    fun logout(
        id: UUID,
        userId: UUID
    ) {
        if (id != userId) throw UnauthorizedException("userId no coincide")
        val user = usersService.getUser(id)

        user.loggedIn = false
        user.rpta = null
        user.token = null
        user.updatedAt = OffsetDateTime.now()

        userRepository.save(user)
    }
}