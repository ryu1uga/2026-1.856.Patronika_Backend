package pe.edu.ulima.patronika.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.PatternLibrary
import pe.edu.ulima.patronika.database.repository.PatternLibraryRepository
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PatternLibraryRequest
import pe.edu.ulima.patronika.dto.PatternLibraryResponseDto
import pe.edu.ulima.patronika.dto.PatternResponseDto
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID

@Service
class PatternLibraryService(
    private val patternLibraryRepository: PatternLibraryRepository,
    private val patternRepository: PatternRepository,
    private val userRepository: UserRepository
) {
    private fun Pattern.toDto() = PatternResponseDto(
        id = id,
        userId = user.id,
        name = name,
        gridData = gridData,
        width = width,
        height = height,
        isPublic = isPublic,
        publishedAt = publishedAt,
        createdAt = createdAt
    )

    private fun PatternLibrary.toDto() = PatternLibraryResponseDto(
        id = id,
        userId = user.id,
        pattern = pattern.toDto(),
        savedAt = savedAt
    )

    fun savePattern(request: PatternLibraryRequest): PatternLibraryResponseDto {
        val user = userRepository.findById(request.userId)
            .orElseThrow { BadRequestException("Usuario no registrado") }
        val pattern = patternRepository.findById(request.patternId)
            .orElseThrow { BadRequestException("Patrón no registrado") }

        if (patternLibraryRepository.existsByUserIdAndPatternId(request.userId, request.patternId)) {
            throw ConflictException("El patrón ya está en la biblioteca del usuario")
        }

        val entry = PatternLibrary(user = user, pattern = pattern)
        return patternLibraryRepository.save(entry).toDto()
    }

    @Transactional
    fun removePattern(userId: UUID, patternId: UUID) {
        if (!userRepository.existsById(userId)) throw BadRequestException("Usuario no registrado")
        if (!patternRepository.existsById(patternId)) throw BadRequestException("Patrón no registrado")
        if (!patternLibraryRepository.existsByUserIdAndPatternId(userId, patternId)) {
            throw NotFoundException()
        }
        patternLibraryRepository.deleteByUserIdAndPatternId(userId, patternId)
    }

    fun getLibraryByUser(userId: UUID): List<PatternLibraryResponseDto> {
        if (!userRepository.existsById(userId)) throw BadRequestException("Usuario no registrado")
        return patternLibraryRepository.findAllByUserIdOrderBySavedAtDesc(userId).map { it.toDto() }
    }

    fun getAllPatternsOfUser(userId: UUID): List<PatternResponseDto> {
        if (!userRepository.existsById(userId)) throw BadRequestException("Usuario no registrado")

        val ownPatterns = patternRepository.findAllByUserIdOrderByCreatedAtDesc(userId)

        // Patrones guardados en biblioteca que NO son propios
        val savedOtherPatterns = patternLibraryRepository
            .findAllByUserIdOrderBySavedAtDesc(userId)
            .map { it.pattern }
            .filter { it.user.id != userId }

        return (ownPatterns + savedOtherPatterns)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
            .map { it.toDto() }
    }
}
