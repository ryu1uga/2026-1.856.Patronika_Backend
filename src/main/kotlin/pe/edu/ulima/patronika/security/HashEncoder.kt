package pe.edu.ulima.patronika.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {
    private val bcrypt = BCryptPasswordEncoder()

    fun encode(raw: String): String = requireNotNull(bcrypt.encode(raw))

    fun matches(raw: String, hashed: String): Boolean = bcrypt.matches(raw, hashed)
}