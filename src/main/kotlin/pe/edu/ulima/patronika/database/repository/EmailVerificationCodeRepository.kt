package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import pe.edu.ulima.patronika.database.model.EmailVerificationCodeEntity
import java.util.UUID

interface EmailVerificationCodeRepository: JpaRepository<EmailVerificationCodeEntity, UUID> {
    fun findByEmailAndHashedCode(email: String, hashedCode: String): EmailVerificationCodeEntity?

    @Modifying
    @Query("DELETE FROM EmailVerificationCodeEntity e WHERE e.email = :email")
    fun deleteByEmail(email: String)
}