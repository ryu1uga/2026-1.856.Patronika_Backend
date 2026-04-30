package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.exception.NotFoundException
import java.time.LocalDateTime
import java.util.UUID

@Service
class PatternsService (
    private val patternRepository: PatternRepository,
) {
    fun getAll(): List<Pattern> = patternRepository.findAll()

    fun getPattern(id: UUID): Pattern {
        return patternRepository.findById(id).orElseThrow { NotFoundException() }
    }

    fun updatePattern(
        id: UUID,
        req: Pattern,
    ) {
        val pattern = getPattern(id)

        pattern.name = req.name
        pattern.imageUrl = req.imageUrl
        pattern.gridData = req.gridData
        pattern.size = req.size
        pattern.difficulty = req.difficulty
        pattern.technique = req.technique
        pattern.isPublic = req.isPublic
        pattern.publishedAt = req.publishedAt

        patternRepository.save(pattern)
    }

    fun deletePattern(id: UUID) {
        if (!patternRepository.existsById(id)) throw NotFoundException()
        patternRepository.deleteById(id)
    }
}