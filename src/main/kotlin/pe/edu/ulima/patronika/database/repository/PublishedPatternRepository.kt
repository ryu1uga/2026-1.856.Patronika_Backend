package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.PublishedPattern
import java.util.UUID

interface PublishedPatternRepository : JpaRepository<PublishedPattern, UUID> {
    fun findAllByUserId(userId: UUID): List<PublishedPattern>
    fun findAllByPatternId(patternId: UUID): List<PublishedPattern>
    fun existsByUserIdAndPatternId(userId: UUID, patternId: UUID): Boolean
}
