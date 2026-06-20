package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PatternCreateRequest
import pe.edu.ulima.patronika.dto.PatternRequest
import pe.edu.ulima.patronika.dto.PatternResponseDto
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID

@Service
class PatternsService (
    private val patternRepository: PatternRepository,
    private val userRepository: UserRepository,
    private val imageConvolutionService: ImageConvolutionService
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

    fun getAll(): List<PatternResponseDto> =
        patternRepository.findAllByOrderByCreatedAtDesc().map { it.toDto() }

    fun getAllByUserId(userId: UUID): List<PatternResponseDto> {
        if (!userRepository.existsById(userId)) {
            throw BadRequestException("Usuario no registrado")
        }

        return patternRepository.findAllByUserIdOrderByCreatedAtDesc(userId).map { it.toDto() }
    }

    fun getPattern(id: UUID): PatternResponseDto {
        return patternRepository.findById(id).orElseThrow { NotFoundException() }.toDto()
    }

    fun getPatternEntity(id: UUID): Pattern {
        return patternRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    fun insertPattern(
        userId: UUID,
        patternRequest: PatternCreateRequest,
        image: MultipartFile? = null
    ): PatternResponseDto {
        val user = getUser(userId)

        // Procesar imagen si se subió una
        val gridData: String? = image?.let { img ->
            if (!img.isEmpty) {
                imageConvolutionService.imageToGridData(img, patternRequest.width, patternRequest.height)
            } else null
        }

        val pattern = Pattern(
            name = patternRequest.name,
            width = patternRequest.width,
            height = patternRequest.height,
            user = user,
            gridData = gridData
        )
        return patternRepository.save(pattern).toDto()
    }
    fun updatePattern(
        id: UUID,
        req: PatternRequest
    ) {
        val pattern = getPatternEntity(id)

        pattern.name = req.name
        pattern.width = req.width
        pattern.height = req.height

        patternRepository.save(pattern)
    }

    fun deletePattern(id: UUID) {
        if (!patternRepository.existsById(id)) throw NotFoundException()
        patternRepository.deleteById(id)
    }

    fun getPatternsOfUser(userId: UUID): List<Pattern> {
        if (!userRepository.existsById(userId)) throw BadRequestException("Usuario no registrado")
        return patternRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
    }
}