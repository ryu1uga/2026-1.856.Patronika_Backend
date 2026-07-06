package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Comment
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.CommentRepository
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.CommentRequest
import pe.edu.ulima.patronika.dto.CommentResponse
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.time.Instant
import java.util.UUID

@Service
class CommentsService (
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val publicationRepository: PublicationRepository
) {
    private fun Comment.toDto() = CommentResponse(
        id = id,
        userId = user.id,
        publicationId = publication.id,
        content = content,
        reportCount = reportCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun getAll(): List<CommentResponse> = commentRepository.findAll().map { it.toDto() }

    fun getComment(id: UUID): CommentResponse {
        return commentRepository.findById(id).orElseThrow { NotFoundException() }.toDto()
    }

    private fun getCommentEntity(id: UUID): Comment {
        return commentRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    private fun getPublication(publicationId: UUID): Publication {
        return publicationRepository.findById(publicationId).orElseThrow { BadRequestException("Publicación no registrada") }
    }

    fun insertComment(
        userId: UUID,
        commentRequest: CommentRequest
    ): CommentResponse {
        val user = getUser(userId)
        val publicationId = commentRequest.publicationId
            ?: throw BadRequestException("publicationId es requerido")
        val publication = getPublication(publicationId)

        val comment = Comment(
            content = commentRequest.content,
            user = user,
            publication = publication
        )

        return commentRepository.save(comment).toDto()
    }

    fun updateComment(
        id: UUID,
        req: CommentRequest
    ) {
        val comment = getCommentEntity(id)

        comment.content = req.content
        comment.updatedAt = Instant.now()

        commentRepository.save(comment)
    }

    fun reportComment(id: UUID): CommentResponse {
        val comment = getCommentEntity(id)
        comment.reportCount += 1
        return commentRepository.save(comment).toDto()
    }

    fun clearReports(id: UUID): CommentResponse {
        val comment = getCommentEntity(id)
        comment.reportCount = 0
        return commentRepository.save(comment).toDto()
    }

    fun deleteComment(id: UUID) {
        if (!commentRepository.existsById(id)) throw NotFoundException()
        commentRepository.deleteById(id)
    }
}