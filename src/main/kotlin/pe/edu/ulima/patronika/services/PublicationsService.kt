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
import pe.edu.ulima.patronika.dto.PublicationResponse
import pe.edu.ulima.patronika.dto.UserSummary
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import java.util.UUID
import java.time.Instant

@Service
class PublicationsService (
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val patternRepository: PatternRepository,
    private val cloudinaryService: CloudinaryService,
    private val publishedPatternRepository: PublishedPatternRepository,
    private val emailService: EmailService
) {
    private fun Publication.toDto() = PublicationResponse(
        id = id,
        user = UserSummary(
            id = user.id,
            username = user.username,
            profileImageUrl = user.profileImageUrl
        ),
        patternId = pattern.id,
        description = description,
        technique = technique,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        reportCount = reportCount
    )

    fun getAll(): List<PublicationResponse> =
        publicationRepository.findAllByOrderByPublishedAtDesc().map { it.toDto() }

    fun getPublication(id: UUID): PublicationResponse {
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
    ): PublicationResponse {
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

    fun adminDeletePublication(id: UUID, adminId: UUID, reason: String) {
        val admin = userRepository.findById(adminId).orElseThrow { BadRequestException("Admin no encontrado") }
        if (admin.isAdmin != true) throw UnauthorizedException()

        val publication = getPublicationEntity(id)
        val ownerEmail = publication.user.email
        val ownerUsername = publication.user.username

        publicationRepository.delete(publication)

        emailService.sendPublicationDeletedEmail(ownerEmail, ownerUsername, reason)
    }

    fun reportPublication(id: UUID) {
        val publication = getPublicationEntity(id)
        publication.reportCount++
        publicationRepository.save(publication)
    }

    fun clearReports(id: UUID) {
        val publication = getPublicationEntity(id)
        publication.reportCount = 0
        publicationRepository.save(publication)
    }
}