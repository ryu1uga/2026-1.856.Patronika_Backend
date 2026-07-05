package pe.edu.ulima.patronika.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import pe.edu.ulima.patronika.exception.UnauthorizedException
import java.util.Base64
import java.util.Date
import java.util.UUID

class JwtServiceTest {
    // Generamos un secreto Base64 válido de 256 bits para HS256.
    private val testSecret: String = Base64.getEncoder()
        .encodeToString(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).encoded)

    private lateinit var jwtService: JwtService
    private val userId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(testSecret)
    }

    //Generacion de tokens
    @Test
    fun generateAccessToken_devuelveTokenValido() {
        val token = jwtService.generateAccessToken(userId)

        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertEquals(3, token.split(".").size)
    }

    @Test
    fun generateRefreshToken_difiereDelAccessToken() {
        val access = jwtService.generateAccessToken(userId)
        val refresh = jwtService.generateRefreshToken(userId)

        assertNotEquals(access, refresh)
    }

    //Validacion Tipo token

    @Test
    fun validateAccessToken_tokenValido() {
        val token = jwtService.generateAccessToken(userId)
        assertTrue(jwtService.validateAccessToken(token))
    }

    @Test
    @DisplayName("validateAccessToken es false si se le pasa un refresh token")
    fun validateAccessToken_conRefreshToken() {
        val refresh = jwtService.generateRefreshToken(userId)
        assertFalse(jwtService.validateAccessToken(refresh))
    }

    @Test
    @DisplayName("validateRefreshToken es true para un refresh token recién generado")
    fun validateRefreshToken_tokenValido() {
        val token = jwtService.generateRefreshToken(userId)
        assertTrue(jwtService.validateRefreshToken(token))
    }

    @Test
    @DisplayName("validateRefreshToken es false si se le pasa un access token")
    fun validateRefreshToken_conAccessToken() {
        val access = jwtService.generateAccessToken(userId)
        assertFalse(jwtService.validateRefreshToken(access))
    }

    //Tokens Invalidos
    @Test
    fun validateAccessToken_tokenCorrupto() {
        assertFalse(jwtService.validateAccessToken("esto-no-es-un-jwt"))
    }

    @Test
    fun validateAccessToken_tokenVacio() {
        assertFalse(jwtService.validateAccessToken(""))
    }

    @Test
    fun validateAccessToken_firmadoConOtroSecreto() {
        val otroSecreto = Base64.getEncoder()
            .encodeToString(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).encoded)
        val otroServicio = JwtService(otroSecreto)

        val tokenAjeno = otroServicio.generateAccessToken(userId)

        assertFalse(jwtService.validateAccessToken(tokenAjeno))
    }

    @Test
    fun getUserIdFromTokenBearer_devuelveId() {
        val token = jwtService.generateAccessToken(userId)

        val idExtraido = jwtService.getUserIdFromToken("Bearer $token")

        assertEquals(userId, idExtraido)
    }

    @Test
    fun getUserIdFromToken_sinBearer_devuelveId() {
        val token = jwtService.generateAccessToken(userId)

        val idExtraido = jwtService.getUserIdFromToken(token)

        assertEquals(userId, idExtraido)
    }

    @Test
    fun getUserIdFromToken_tokenInvalido() {
        assertThrows(UnauthorizedException::class.java) {
            jwtService.getUserIdFromToken("Invalid token")
        }
    }

    @Test
    fun validateAccessToken_tipoInvalido() {
        val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret))
        val now = Date()

        val tokenConTipoNull = Jwts.builder()
            .subject(userId)
            .claim("type", null)
            .issuedAt(now)
            .expiration(Date(now.time + 60000))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()

        assertFalse(jwtService.validateAccessToken(tokenConTipoNull))
    }

    @Test
    fun validateRefreshToken_tipoInvalido() {
        val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret))
        val now = Date()

        val tokenConTipoNull = Jwts.builder()
            .subject(userId)
            .claim("type", null)
            .issuedAt(now)
            .expiration(Date(now.time + 60000))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()

        assertFalse(jwtService.validateRefreshToken(tokenConTipoNull))
    }

    //Expiracion de tokens
    @Test
    fun validateAccessToken_tokenExpirado() {
        // usando la misma clave secreta, para simular el paso del tiempo
        // sin depender de Thread.sleep (evitamos tests lentos/flaky).
        val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(testSecret))
        val now = Date()
        val yaExpirado = Date(now.time - 1000) //token vencido

        val tokenExpirado = Jwts.builder()
            .subject(userId)
            .claim("type", "access")
            .issuedAt(Date(now.time - 2000))
            .expiration(yaExpirado)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()

        assertFalse(jwtService.validateAccessToken(tokenExpirado))
    }
}