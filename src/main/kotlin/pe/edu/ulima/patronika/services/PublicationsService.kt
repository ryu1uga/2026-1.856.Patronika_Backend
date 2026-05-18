package pe.edu.ulima.patronika.services

import org.hibernate.query.range.Range.pattern
import org.springframework.stereotype.Service
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
    private val patternRepository: PatternRepository
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
        publicationRequest: PublicationRequest
    ): Publication {
        val user = getUser(userId)
        val pattern = getPattern(patternId)

        val publication = Publication(
            description = publicationRequest.description,
            publishedAt = Instant.now(),
            user = user,
            pattern = pattern
        )

        return publicationRepository.save(publication)
    }

    fun updatePublication(
    id: UUID,
    req: PublicationRequest
    ) {
        val publication = getPublication(id)

        publication.description = req.description

        publicationRepository.save(publication)
    }

    fun deletePublication(id: UUID) {
        if (!publicationRepository.existsById(id)) throw NotFoundException()
        publicationRepository.deleteById(id)
    }
}