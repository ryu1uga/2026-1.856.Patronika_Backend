package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.model.PublishedPattern
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.database.repository.PublishedPatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.dto.PublicationResponseDto
import pe.edu.ulima.patronika.dto.UserSummaryDto
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID
import java.time.Instant

@Service
class PublicationsService (
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val patternRepository: PatternRepository,
    private val cloudinaryService: CloudinaryService,
    private val publishedPatternRepository: PublishedPatternRepository
) {
    private fun Publication.toDto() = PublicationResponseDto(
        id = id,
        user = UserSummaryDto(
            id = user.id,
            username = user.username,
            profileImageUrl = user.profileImageUrl
        ),
        patternId = pattern.id,
        description = description,
        technique = technique,
        imageUrl = imageUrl,
        publishedAt = publishedAt
    )

    fun getAll(): List<PublicationResponseDto> =
        publicationRepository.findAllByOrderByPublishedAtDesc().map { it.toDto() }

    fun getPublication(id: UUID): PublicationResponseDto {
        return publicationRepository.findById(id).orElseThrow { NotFoundException() }.toDto()
    }

    private fun getPublicationEntity(id: UUID): Publication {
        return publicationRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    private fun getPattern(patternId: UUID): Pattern {
        return patternRepository.findById(patternId).orElseThrow { BadRequestException("Patrón no registrado") }
    }

    fun insertPublication(
        publicationRequest: PublicationRequest,
        file: MultipartFile?
    ): PublicationResponseDto {
        val user = getUser(publicationRequest.userId)
        val pattern = getPattern(publicationRequest.patternId)

        val publication = Publication(
            description = publicationRequest.description,
            technique = publicationRequest.technique,
            publishedAt = Instant.now(),
            user = user,
            pattern = pattern
        )

        // SI VIENE UN ARCHIVO NUEVO
        if (file != null && !file.isEmpty) {
            // 1. Si ya tenía una imagen previa en Cloudinary, la borramos
            publication.imageUrl?.let { oldUrl ->
                if (oldUrl.contains("cloudinary.com")) {
                    cloudinaryService.deleteImage(oldUrl)
                }
            }
            // 2. Subimos la nueva imagen
            publication.imageUrl = cloudinaryService.uploadImage(file, folder = "patterns")
        }

        val saved = publicationRepository.save(publication)

        // Registrar en published_patterns si aún no existe esta combinación
        if (!publishedPatternRepository.existsByUserIdAndPatternId(user.id!!, pattern.id!!)) {
            publishedPatternRepository.save(
                PublishedPattern(user = user, pattern = pattern, publishedAt = saved.publishedAt ?: Instant.now())
            )
        }

        return saved.toDto()
    }

    fun updatePublication(
    id: UUID,
    req: PublicationRequest,
    file: MultipartFile?
    ) {
        val publication = getPublicationEntity(id)

        if (file != null && !file.isEmpty) {
            // Si ya tenía una imagen previa en Cloudinary, la borramos
            publication.imageUrl?.let { oldUrl ->
                if (oldUrl.contains("cloudinary.com")) {
                    cloudinaryService.deleteImage(oldUrl)
                }
            }
            publication.imageUrl = cloudinaryService.uploadImage(file, folder = "patterns")
        }

        publication.description = req.description
        publication.technique = req.technique

        publicationRepository.save(publication)
    }

    fun deletePublication(id: UUID) {
        val publication = getPublicationEntity(id)
        publicationRepository.delete(publication)
    }
}