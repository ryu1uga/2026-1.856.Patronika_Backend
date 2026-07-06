package pe.edu.ulima.patronika.security

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.BadCredentialsException
import pe.edu.ulima.patronika.database.model.EmailVerificationCodeEntity
import pe.edu.ulima.patronika.database.model.RefreshTokenEntity
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.EmailVerificationCodeRepository
import pe.edu.ulima.patronika.database.repository.RefreshTokenRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.ForgotPasswordRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.services.EmailService
import pe.edu.ulima.patronika.services.UsersService
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    @Mock
    private lateinit var jwtService: JwtService
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var usersService: UsersService
    @Mock
    private lateinit var hashEncoder: HashEncoder
    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Mock
    private lateinit var emailVerificationCodeRepository: EmailVerificationCodeRepository
    @Mock
    private lateinit var emailService: EmailService

    private lateinit var authService: AuthService

    private val codeExpiryMs = 10 * 60 * 1000L   // 10 minutos, igual que el correo real
    private val tokenExpiryMs = 30 * 60 * 1000L

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            jwtService = jwtService,
            userRepository = userRepository,
            usersService = usersService,
            hashEncoder = hashEncoder,
            refreshTokenRepository = refreshTokenRepository,
            emailVerificationCodeRepository = emailVerificationCodeRepository,
            emailService = emailService,
            codeExpiryMs = codeExpiryMs,
            tokenExpiryMs = tokenExpiryMs
        )
    }

    private fun buildUser(
        id: UUID = UUID.randomUUID(),
        username: String = "user",
        email: String = "user@example.com",
        hashedPassword: String = "hashed-real",
        status: Int = 0,
        suspensionStartDate: LocalDate? = null,
        suspendUserEndDate: LocalDate? = null,
        suspensionReason: String? = null
    ) = User(
        id = id,
        username = username,
        email = email,
        hashedPassword = hashedPassword,
        status = status,
        suspensionStartDate = suspensionStartDate,
        suspensionEndDate = suspendUserEndDate,
        suspensionReason = suspensionReason,
        )

    private fun sha256Base64(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return Base64.getEncoder().encodeToString(digest.digest(raw.encodeToByteArray()))
    }

    //Login
    @Test
    fun login_flujoCompleto_DevuelveToken(){
        val user = buildUser(status = 0)
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        whenever(hashEncoder.matches("clave123", user.hashedPassword)).thenReturn(true)
        whenever(jwtService.generateAccessToken(user.id.toString())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(user.id.toString())).thenReturn("refresh-token")
        whenever(jwtService.refreshTokenValidityMs).thenReturn(tokenExpiryMs)


        val result =authService.login("user", "clave123")

        assertEquals(user.id.toString(),result.userId)
        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)


        assertTrue(user.loggedIn)
        assertEquals(0, user.status)
        verify(userRepository).findByUsername("user")
        verify(refreshTokenRepository).save(any())
    }

    @Test
    fun login_suspensionVencida(){
        val user = buildUser(status = 1,
            suspensionStartDate = LocalDate.now().minusDays(10),
            suspendUserEndDate = LocalDate.now().minusDays(1),
            suspensionReason = "spam"
        )
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        whenever(hashEncoder.matches("clave123", user.hashedPassword)).thenReturn(true)
        whenever(jwtService.generateAccessToken(user.id.toString())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(user.id.toString())).thenReturn("refresh-token")
        whenever(jwtService.refreshTokenValidityMs).thenReturn(tokenExpiryMs)

        val result = authService.login("user", "clave123")

        assertEquals(0, user.status)
        assertNull(user.suspensionStartDate)
        assertNull(user.suspensionEndDate)
        assertNull(user.suspensionReason)
        assertNull(result.suspensionDaysRemaining)
    }

    @Test
    fun login_suspensionActiva(){
        val user = buildUser(status = 1, suspendUserEndDate = LocalDate.now().plusDays(1))
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        whenever(hashEncoder.matches("clave123", user.hashedPassword)).thenReturn(true)
        whenever(jwtService.generateAccessToken(user.id.toString())).thenReturn("access-token")
        whenever(jwtService.generateRefreshToken(user.id.toString())).thenReturn("refresh-token")
        whenever(jwtService.refreshTokenValidityMs).thenReturn(tokenExpiryMs)

        val result = authService.login("user", "clave123")

        assertEquals(1, user.status)
        assertEquals(LocalDate.now().plusDays(1), user.suspensionEndDate)
        assertEquals(1, result.suspensionDaysRemaining)
    }

    @Test
    fun login_UsuarioInexistente(){
        whenever(userRepository.findByUsername("fakeUser")).thenReturn(null)

        val error = assertThrows(BadCredentialsException::class.java) {
            authService.login("fakeUser", "123")
        }
        assertEquals("Usuario o contraseña incorrectos", error.message)
    }

    @Test
    fun login_PasswordIncorrecto(){
        val user = buildUser()
        whenever(userRepository.findByUsername("user")).thenReturn(user)
        whenever(hashEncoder.matches("incorrecto", user.hashedPassword)).thenReturn(false)

        val error = assertThrows(BadCredentialsException::class.java) {
            authService.login("user", "incorrecto")
        }

        assertEquals("Usuario o contraseña incorrectos", error.message)
        verify(userRepository, never()).save(any())

    }

    //renovacion de token / sesion
    @Test
    fun renovacionDeToken(){
        val user = buildUser()
        val rawToken = "refresh-raw-valido"
        val hashed = sha256Base64(rawToken)
        whenever(jwtService.validateRefreshToken(rawToken)).thenReturn(true)
        whenever(jwtService.getUserIdFromToken(rawToken)).thenReturn(user.id.toString())
        whenever(usersService.getUser(user.id!!)).thenReturn(user)
        whenever(refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)).thenReturn(RefreshTokenEntity(userId = user.id!!, token = hashed))
        whenever(jwtService.generateAccessToken(user.id.toString())).thenReturn("nuevo-access")
        whenever(jwtService.generateRefreshToken(user.id.toString())).thenReturn("nuevo-refresh")
        whenever(jwtService.refreshTokenValidityMs).thenReturn(tokenExpiryMs)

        val result =authService.refresh(rawToken)
        assertEquals("nuevo-access", result["accessToken"])
        assertEquals("nuevo-refresh", result["refreshToken"])
        // El token usado debe ser revocado (single-use) antes de emitir uno nuevo
        verify(refreshTokenRepository).deleteByUserIdAndToken(user.id!!, hashed)
        verify(refreshTokenRepository).save(any())

    }
    @Test
    fun renovaciónDeTokenInvalido(){
        whenever(jwtService.validateRefreshToken("token-invalido")).thenReturn(false)

        val error = assertThrows(UnauthorizedException::class.java) {
            authService.refresh("token-invalido")
        }
        assertEquals("Refresh token inválido", error.message)
        verifyNoInteractions(refreshTokenRepository)

    }

    @Test
    fun TokenNoReconocido_EnBasedeDatos(){
        val user = buildUser()
        val rawToken = "refresh-raw-reutilizado"
        val hashed = sha256Base64(rawToken)

        whenever(jwtService.validateRefreshToken(rawToken)).thenReturn(true)
        whenever(jwtService.getUserIdFromToken(rawToken)).thenReturn(user.id.toString())
        whenever(usersService.getUser(user.id!!)).thenReturn(user)
        whenever(refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)).thenReturn(null)

        val error = assertThrows(UnauthorizedException::class.java) {
            authService.refresh(rawToken)
        }
        assertEquals("Refresh token no reconocido (puede haber sido usado o expirado)",error.message)
        verify(refreshTokenRepository, never()).deleteByUserIdAndToken(any(), any())
    }

//Logout
    @Test
    fun Logout_FlujoCompleto(){
        val user = buildUser().apply { loggedIn = true }
        val rawToken = "refresh-de-sesion-activa"
        val hashed = sha256Base64(rawToken)
        whenever(usersService.getUser(user.id!!)).thenReturn(user)
        whenever(refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)).thenReturn(RefreshTokenEntity(userId = user.id!!, token = hashed))

        authService.logout(user.id!!, rawToken)
        assertFalse(user.loggedIn)
        verify(refreshTokenRepository).deleteByUserIdAndToken(user.id!!, hashed)
        verify(userRepository).save(user)
    }

    @Test
    fun Logout_TokenInvalido(){
        val user = buildUser().apply { loggedIn = true }
        val rawToken = "token-expirado"
        val hashed = sha256Base64(rawToken)

        whenever(usersService.getUser(user.id!!)).thenReturn(user)
        whenever(refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)).thenReturn(null)

        assertThrows(UnauthorizedException::class.java) {
            authService.logout(user.id!!, rawToken)
        }
        assertTrue(user.loggedIn, "El estado de sesión no debe cambiar si el token no es válido")
        verify(userRepository, never()).save(any())
    }

//Codigo de verificacion en registro
    @Test
    fun requestVerficationCode_FlujoCompleto(){
        whenever(userRepository.findByEmail("unknown@email.com")).thenReturn(null)
        authService.requestVerificationCode("unknown@email.com")

        verify(emailVerificationCodeRepository).deleteByEmail("unknown@email.com")
        verify(emailVerificationCodeRepository).save(any())
        verify(emailService).sendVerificationCode(eq("unknown@email.com"), any())
    }

    @Test
    fun requestVerficationCode_EmailYaRegistrado(){
        whenever(userRepository.findByEmail("user@email.com")).thenReturn(buildUser())
        val error = assertThrows(ConflictException::class.java) {
            authService.requestVerificationCode("user@email.com")
        }
        assertEquals("El correo ya está registrado", error.message)
        verifyNoInteractions(emailService)
    }

//Codigo de verificacion a email existente
    @Test
    fun requestVerficationCodeAemailExistente_FlujoCompleto(){
        val user = buildUser(email = "user@gmail.com")
        whenever(userRepository.findByEmail(user.email)).thenReturn(buildUser())

        authService.requestVerificationCodeOnExistingEmail(user.email)

        verify(emailVerificationCodeRepository).deleteByEmail(user.email)
        verify(emailVerificationCodeRepository).save(any())   //
        verify(emailService).sendVerificationCode(eq(user.email), any())
    }

    @Test
    fun requestVerficationCodeAemailExistente_EmailInexistente(){
        whenever(userRepository.findByEmail(any())).thenReturn(null)
        val error = assertThrows(NotFoundException::class.java) {
            authService.requestVerificationCodeOnExistingEmail("user@email.com")
        }
        assertEquals("El correo no está registrado",error.message)
        verifyNoInteractions(emailService)
    }



//Verificar Codigo
    @Test
    fun verficiarCodigo_FlujoCompleto(){
        val email = "user@email.com"
        val code = "1234"
        val hashed = sha256Base64(code)
        val user = EmailVerificationCodeEntity(email = email, hashedCode = hashed, expiresAt = Instant.now().plusSeconds(60))
        whenever(emailVerificationCodeRepository.findByEmailAndHashedCode(email, hashed)).thenReturn(user)
        assertDoesNotThrow {authService.verifyCode(email, code)}
        verify(emailVerificationCodeRepository).delete(user)
    }

    @Test
    fun verficiarCodigo_CodigoIncorrecto(){
        whenever(emailVerificationCodeRepository.findByEmailAndHashedCode(any(), any())).thenReturn(null)
        val error = assertThrows(BadRequestException::class.java) {
            authService.verifyCode("user@email.com","1234")
        }
        assertEquals("Código inválido o expirado", error.message)
    }
    @Test
    fun verficiarCodigo_CodigoExpirado(){
        val email = "user@email.com"
        val code = "1234"
        val hashed = sha256Base64(code)
        //poner expirado hace 1 segundo
        val user = EmailVerificationCodeEntity(email = email, hashedCode = hashed, expiresAt = Instant.now().minusSeconds(1))

        whenever(emailVerificationCodeRepository.findByEmailAndHashedCode(email, hashed))
            .thenReturn(user)
        val error = assertThrows(BadRequestException::class.java) {
            authService.verifyCode(email, code)
        }
        assertEquals("El código ha expirado", error.message)
        verify(emailVerificationCodeRepository).delete(user)
    }

    //cambio contraseña
    @Test
    fun CambioContraseña_FlujoCompleto(){
        val user = buildUser(hashedPassword = "viejo_Hash")
        val req = ForgotPasswordRequest(email = user.email, password = "NuevaContraseña123")
        whenever(userRepository.findByEmail(user.email)).thenReturn(user)
        whenever(hashEncoder.encode("NuevaContraseña123")).thenReturn("hash_nuevo")

        authService.changePassword(req)
        assertEquals("hash_nuevo", user.hashedPassword)
        verify(userRepository).save(user)
    }

    @Test
    fun CambioContraseña_EmailInexistente(){
        val req = ForgotPasswordRequest(email = "inexistente@email.com", password = "123")
        whenever(userRepository.findByEmail(req.email)).thenReturn(null)

        val error = assertThrows(NotFoundException::class.java) {
            authService.changePassword(req)
        }
        assertEquals("No existe un usuario con ese correo", error.message)
    }








}