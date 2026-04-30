package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID

@Service
class PublicationsService (
    private val publicationRepository: PublicationRepository
) {
    fun getAll(): List<Publication> = publicationRepository.findAll()

    fun getPublication(id: UUID): Publication {
        return publicationRepository.findById(id).orElseThrow { NotFoundException() }
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