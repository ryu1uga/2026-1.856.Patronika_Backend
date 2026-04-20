package pe.edu.ulima.patronika.security

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.repository.UserRepository
import java.util.*

@Service
class HeaderService(
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder
) {
    data class AuthenticatedUser(
        val userId: UUID,
        val rpta: String,
        val token: String
    )

    fun authenticate(
        userIdHeader: String,
        rptaHeader: String,
        tokenHeader: String
    ): AuthenticatedUser? {
        val userId = try {
            UUID.fromString(userIdHeader)
        } catch (_: Exception) {
            return null
        }

        val user = userRepository.findById(userId).orElse(null) ?: return null

        if (!hashEncoder.matches(rptaHeader, user.rpta ?: "")) return null

        if (!hashEncoder.matches(tokenHeader, user.token ?: "")) return null

        return AuthenticatedUser(
            userId = userId,
            rpta = rptaHeader,
            token = tokenHeader
        )
    }
}