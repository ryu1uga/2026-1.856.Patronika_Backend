package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.Comment
import java.util.UUID

interface CommentRepository: JpaRepository<Comment, UUID> {
}