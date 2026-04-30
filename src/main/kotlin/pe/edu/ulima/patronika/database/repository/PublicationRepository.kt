package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.Publication
import java.util.UUID

interface PublicationRepository: JpaRepository<Publication, UUID> {
}