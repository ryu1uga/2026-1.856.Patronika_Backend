package pe.edu.ulima.patronika.database.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(name = "email_verification_codes")
data class EmailVerificationCodeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val email: String = "",

    @Column(name = "hashed_code", nullable = false)
    val hashedCode: String = "",

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant = Instant.now(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)