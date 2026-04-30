package pe.edu.ulima.itlab.security

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.edu.ulima.patronika.database.model.RefreshTokenEntity
import pe.edu.ulima.patronika.database.repository.RefreshTokenRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.security.HashEncoder
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
    private val refreshTokenRepository: RefreshTokenRepository
) {
    @Transactional
    fun login(username: String, password: String): Map<String, String> {
        val user = userRepository.findByUsername(username)
            ?: throw BadCredentialsException("Usuario o contraseña incorrectos")

        if(!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Usuario o contraseña incorrectos")
        }

        val newAccessToken = jwtService.generateAccessToken(user.id.toString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString())

        storeRefreshToken(user.id!!, newRefreshToken)

        user.status = 0
        usersRepository.save(user)

        return mapOf(
            "userId" to user.id.toString(),
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )
    }

    @Transactional
    fun refresh(refreshToken: String): Map<String, String> {
        if(!jwtService.validateRefreshToken(refreshToken)) {
            throw UnauthorizedException("Refresh token inválido")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = usersService.getUser(UUID.fromString(userId))

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)
            ?: throw UnauthorizedException("Refresh token no reconocido (puede haber sido usado o expirado)")

        refreshTokenRepository.deleteByUserIdAndToken(user.id, hashed)

        val newAccessToken = jwtService.generateAccessToken(user.id.toString())
        val newRefreshToken = jwtService.generateRefreshToken(user.id.toString())

        storeRefreshToken(user.id, newRefreshToken)

        return mapOf(
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )
    }

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

    @Transactional
    fun logout(id: UUID, refreshToken: String) {
        val user = usersService.getUser(id)

        val hashed = hashToken(refreshToken)
        refreshTokenRepository.findByUserIdAndToken(user.id!!, hashed)
            ?: throw UnauthorizedException("Refresh token no reconocido (puede haber sido usado o expirado)")

        refreshTokenRepository.deleteByUserIdAndToken(user.id, hashed)

        user.loggedIn = false
        usersRepository.save(user)
    }
}