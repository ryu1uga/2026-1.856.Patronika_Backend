package pe.edu.ulima.patronika.services

import org.hibernate.query.range.Range.pattern
import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID
import java.time.Instant

@Service
class PublicationsService (
    private val publicationRepository: PublicationRepository,
    private val usersService: UsersService,
    private val patternsService: PatternsService
) {
    fun getAll(): List<Publication> = publicationRepository.findAll()

    fun getPublication(id: UUID): Publication {
        return publicationRepository.findById(id).orElseThrow { NotFoundException() }
    }

    fun insertPublication(
        userId: UUID,
        patternId: UUID,
        publicationRequest: PublicationRequest
    ): Publication {
        val user = usersService.getUser(userId)
        val pattern = patternsService.getPattern(patternId)

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