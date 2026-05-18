package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Comment
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.CommentRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.CommentRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.time.Instant
import java.util.UUID

@Service
class CommentsService (
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) {
    fun getAll(): List<Comment> = commentRepository.findAll()

    fun getComment(id: UUID): Comment {
        return commentRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    fun insertComment(
        userId: UUID,
        commentRequest: CommentRequest
    ): Comment {
        val user = getUser(userId)

        val comment = Comment(
            content = commentRequest.content,
            user = user
        )

        return commentRepository.save(comment)
    }

    fun updateComment(
        id: UUID,
        req: CommentRequest
    ) {
        val comment = getComment(id)

        comment.content = req.content
        comment.updatedAt = Instant.now()

        commentRepository.save(comment)
    }

    fun deleteComment(id: UUID) {
        if(!commentRepository.existsById(id)) throw NotFoundException()
        commentRepository.deleteById(id)
    }
}