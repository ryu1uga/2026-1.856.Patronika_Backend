package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID
import java.time.Instant

@Service
class PublicationsService (
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val patternRepository: PatternRepository,
    private val cloudinaryService: CloudinaryService
) {
    fun getAll(): List<Publication> = publicationRepository.findAll()

    fun getPublication(id: UUID): Publication {
        return publicationRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    private fun getPattern(patternId: UUID): Pattern {
        return patternRepository.findById(patternId).orElseThrow { BadRequestException("Patrón no registrado") }
    }

    fun insertPublication(
        userId: UUID,
        patternId: UUID,
        publicationRequest: PublicationRequest,
        file: MultipartFile?
    ): Publication {
        val user = getUser(userId)
        val pattern = getPattern(patternId)

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

        return publicationRepository.save(publication)
    }

    fun updatePublication(
    id: UUID,
    req: PublicationRequest,
    file: MultipartFile?
    ) {
        val publication = getPublication(id)

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
        val publication = getPublication(id) // Usamos getPublication para asegurar que existe y obtener sus datos
        publicationRepository.delete(publication)
    }
}