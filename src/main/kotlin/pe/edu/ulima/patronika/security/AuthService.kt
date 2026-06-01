package pe.edu.ulima.patronika.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.edu.ulima.patronika.database.model.EmailVerificationCodeEntity
import pe.edu.ulima.patronika.database.model.RefreshTokenEntity
import pe.edu.ulima.patronika.database.repository.EmailVerificationCodeRepository
import pe.edu.ulima.patronika.database.repository.RefreshTokenRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.UserRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.services.EmailService
import pe.edu.ulima.patronika.services.UsersService
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val usersService: UsersService,
    private val hashEncoder: HashEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationCodeRepository: EmailVerificationCodeRepository,
    private val emailService: EmailService,
    @Value("\${app.verification.code.expiry-ms}") private val codeExpiryMs: Long,
    @Value("\${app.verification.token.expiry-ms}") private val tokenExpiryMs: Long
) {

    // -------------------------
    // Login / Refresh / Logout
    // -------------------------

    @Transactional
    fun login(username: String, password: String): Map<String, String> {
        val user = userRepository.findByUsername(username)
            ?: throw BadCredentialsException("Usuario o contraseña incorrectos")

        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Usuario o contraseña incorrectos")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString())

        storeRefreshToken(user.id!!, newRefreshToken)

        user.status = 0
        userRepository.save(user)

        return mapOf(
            "userId" to user.id.toString(),
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )
    }

    @Transactional
    fun refresh(refreshToken: String): Map<String, String> {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw UnauthorizedException("Refresh token inválido")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = usersService.getUser(UUID.fromString(userId))

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)
            ?: throw UnauthorizedException("Refresh token no reconocido (puede haber sido usado o expirado)")

        refreshTokenRepository.deleteByUserIdAndToken(user.id!!, hashed)

        val newAccessToken = jwtService.generateAccessToken(user.id.toString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString())

        storeRefreshToken(user.id!!, newRefreshToken)

        return mapOf(
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )
    }

    @Transactional
    fun logout(id: UUID, refreshToken: String) {
        val user = usersService.getUser(id)

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)
            ?: throw UnauthorizedException("Refresh token no reconocido (puede haber sido usado o expirado)")

        refreshTokenRepository.deleteByUserIdAndToken(user.id!!, hashed)

        user.loggedIn = false
        userRepository.save(user)
    }

    // -------------------------
    // Registro con verificación
    // -------------------------

    @Transactional
    fun requestVerificationCode(email: String) {
        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("El correo ya está registrado")
        }

        // Borrar códigos previos del mismo email
        emailVerificationCodeRepository.deleteByEmail(email)

        val code = (1000..9999).random().toString()
        val hashed = hashToken(code)
        val expiresAt = Instant.now().plusMillis(codeExpiryMs)

        emailVerificationCodeRepository.save(
            EmailVerificationCodeEntity(
                email = email,
                hashedCode = hashed,
                expiresAt = expiresAt
            )
        )

        emailService.sendVerificationCode(email, code)
    }

    @Transactional
    fun requestVerificationCodeOnExistingEmail(email: String) {
        if (userRepository.findByEmail(email) == null) {
            throw ConflictException("El correo no está registrado")
        }

        // Borrar códigos previos del mismo email
        emailVerificationCodeRepository.deleteByEmail(email)

        val code = (1000..9999).random().toString()
        val hashed = hashToken(code)
        val expiresAt = Instant.now().plusMillis(codeExpiryMs)

        emailVerificationCodeRepository.save(
            EmailVerificationCodeEntity(
                email = email,
                hashedCode = hashed,
                expiresAt = expiresAt
            )
        )

        emailService.sendVerificationCode(email, code)
    }

    @Transactional
    fun verifyCode(email: String, code: String) {
        val hashed = hashToken(code)

        val entity = emailVerificationCodeRepository.findByEmailAndHashedCode(email, hashed)
            ?: throw BadRequestException("Código inválido o expirado")

        if (Instant.now().isAfter(entity.expiresAt)) {
            emailVerificationCodeRepository.delete(entity)
            throw BadRequestException("El código ha expirado")
        }

        emailVerificationCodeRepository.delete(entity)
    }

    @Transactional
    fun changePassword(userRequest: UserRequest) {
        val user = userRepository.findByEmail(userRequest.email)

        user!!.hashedPassword = hashEncoder.encode(userRequest.password)

        userRepository.save(user)
    }

    // -------------------------
    // Helpers privados
    // -------------------------

    private fun storeRefreshToken(userId: UUID, rawRefreshToken: String) {
        val hashed = hashToken(rawRefreshToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                token = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}