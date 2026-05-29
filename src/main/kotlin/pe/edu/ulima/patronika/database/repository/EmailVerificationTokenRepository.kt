package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import pe.edu.ulima.patronika.database.model.EmailVerificationTokenEntity
import java.util.UUID

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, UUID> {
    fun findByHashedToken(hashedToken: String): EmailVerificationTokenEntity?

    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity e WHERE e.email = :email")
    fun deleteByEmail(email: String)
}