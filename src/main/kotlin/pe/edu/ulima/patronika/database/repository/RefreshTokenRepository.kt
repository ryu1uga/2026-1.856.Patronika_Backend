package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.RefreshTokenEntity
import java.util.*

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, UUID> {
    fun findByUserIdAndToken(userId: UUID, hashedToken: String): RefreshTokenEntity?
    fun deleteByUserIdAndToken(userId: UUID, hashedToken: String)
}