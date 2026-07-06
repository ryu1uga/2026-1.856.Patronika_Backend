package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pe.edu.ulima.patronika.database.model.EmailVerificationCodeEntity
import java.time.Instant
import java.util.UUID

interface EmailVerificationCodeRepository: JpaRepository<EmailVerificationCodeEntity, UUID> {
    fun findByEmailAndHashedCode(email: String, hashedCode: String): EmailVerificationCodeEntity?

    @Modifying
    @Query("DELETE FROM EmailVerificationCodeEntity e WHERE e.email = :email")
    fun deleteByEmail(email: String)

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM EmailVerificationCodeEntity e WHERE e.expiresAt < :now")
    fun deleteAllExpired(@Param("now") now: Instant): Int
}
