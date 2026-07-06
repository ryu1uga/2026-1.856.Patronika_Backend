package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pe.edu.ulima.patronika.database.model.RefreshTokenEntity
import java.time.Instant
import java.util.*

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, UUID> {
    fun findByUserIdAndToken(userId: UUID, hashedToken: String): RefreshTokenEntity?
    fun deleteByUserIdAndToken(userId: UUID, hashedToken: String)

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :now")
    fun deleteAllExpired(@Param("now") now: Instant): Int
}
