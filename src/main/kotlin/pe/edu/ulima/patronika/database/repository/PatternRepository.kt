package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.Pattern
import java.util.UUID

interface PatternRepository: JpaRepository<Pattern, UUID> {
    fun findAllByUserId(userId: UUID): List<Pattern>
}