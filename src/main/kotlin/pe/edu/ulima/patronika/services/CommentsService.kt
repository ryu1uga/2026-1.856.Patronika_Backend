package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Comment
import pe.edu.ulima.patronika.database.repository.CommentRepository
import pe.edu.ulima.patronika.dto.CommentRequest
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID

@Service
class CommentsService (
    private val commentRepository: CommentRepository,
) {
    fun getAll(): List<Comment> = commentRepository.findAll()

    fun getComment(id: UUID): Comment {
        return commentRepository.findById(id).orElseThrow { NotFoundException() }
    }

    fun updateComment(
        id: UUID,
        req: CommentRequest
    ) {
        val comment = getComment(id)

        comment.content = req.content

        commentRepository.save(comment)
    }

    fun deleteComment(id: UUID) {
        if(!commentRepository.existsById(id)) throw NotFoundException()
        commentRepository.deleteById(id)
    }
}