package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.PatternLibrary
import java.util.UUID

interface PatternLibraryRepository : JpaRepository<PatternLibrary, UUID> {
    fun findAllByUserIdOrderBySavedAtDesc(userId: UUID): List<PatternLibrary>
    fun existsByUserIdAndPatternId(userId: UUID, patternId: UUID): Boolean
    fun deleteByUserIdAndPatternId(userId: UUID, patternId: UUID)
}
