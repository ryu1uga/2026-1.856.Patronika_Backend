package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pe.edu.ulima.patronika.database.model.Comment
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.CommentRepository
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.CommentRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CommentsServiceTest {

    @Mock
    private lateinit var commentRepository: CommentRepository
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var publicationRepository: PublicationRepository

    @InjectMocks
    private lateinit var commentsService: CommentsService

    private fun buildUser(id: UUID = UUID.randomUUID()) =
        User(id = id, username = "user", email = "user@example.com", hashedPassword = "hash")

    private fun buildPublication(id: UUID = UUID.randomUUID()) =
        Publication(id = id, user = buildUser())

    private fun buildComment(
        id: UUID? = UUID.randomUUID(),
        user: User = buildUser(),
        publication: Publication = buildPublication(),
        content: String = "Un comentario",
        reportCount: Int = 0
    ) = Comment(
        id = id,
        user = user,
        publication = publication,
        content = content,
        reportCount = reportCount
    )

    //insertComment
    @Test
    fun insertComment_FlujoCompleto() {
        val userId = UUID.randomUUID()
        val publicationId= UUID.randomUUID()
        val user = buildUser(userId)
        val publication = buildPublication(userId)
        val req = CommentRequest(content ="patron elegante",publicationId = publicationId)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication))
        whenever(commentRepository.save(any())).thenAnswer {it.arguments[0] }

        val result = commentsService.insertComment(userId, req)

        assertEquals("patron elegante", result.content)
        verify(commentRepository).save(any())

    }

    @Test
    fun insertComment_NecesitaPublicationId() {
        val userId = UUID.randomUUID()
        val user = buildUser(userId)
        val req = CommentRequest(content ="patron elegante",publicationId = null)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val error = assertThrows(BadRequestException::class.java) {
            commentsService.insertComment(userId, req)
        }
        assertEquals("publicationId es requerido", error.message)
        verify(publicationRepository, never()).findById(any())
        verify(commentRepository, never()).save(any())

    }

    //updateComment
    @Test
    fun updateComment_FlujoCompleto() {
        val commentId = UUID.randomUUID()
        val comment = buildComment(commentId, content ="Contenido anticuado")
        val req = CommentRequest(content = "Contenido nuevo")
        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(comment))

        commentsService.updateComment(commentId, req)

        assertEquals("Contenido nuevo", comment.content)
        assertNotNull(comment.updatedAt)
        verify(commentRepository).save(comment)
    }

    //reportComment
    @Test
    fun reportComment_FlujoCompleto() {
        val commentId = UUID.randomUUID()
        val comment = buildComment(commentId, reportCount = 0)

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(comment))
        whenever(commentRepository.save(comment)).thenReturn(comment)

        commentsService.reportComment(commentId)

        assertEquals(1, comment.reportCount)
        verify(commentRepository).save(comment)
    }

    //clearReports
    @Test
    fun clearReports_FlujoCompleto() {
        val commentId = UUID.randomUUID()
        val comment = buildComment(commentId, reportCount = 4)

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(comment))
        whenever(commentRepository.save(comment)).thenReturn(comment)

        commentsService.clearReports(commentId)

        assertEquals(0, comment.reportCount)
        verify(commentRepository).save(comment)
    }

    //deleteComment
    @Test
    fun deleteComment_FlujoCompleto() {
        val commentId = UUID.randomUUID()

        whenever(commentRepository.existsById(commentId)).thenReturn(true)

        commentsService.deleteComment(commentId)

        verify(commentRepository).deleteById(commentId)

    }

    @Test
    fun deleteComment_ComentarioNoExiste() {
        val commentId = UUID.randomUUID()

        whenever(commentRepository.existsById(commentId)).thenReturn(false)

        assertThrows(NotFoundException::class.java) {
            commentsService.deleteComment(commentId)
        }
        verify(commentRepository, never()).deleteById(any())
    }

    //getAll
    @Test
    fun getAll_FlujoCompleto() {
        val comments = listOf(buildComment(), buildComment())
        whenever(commentRepository.findAll()).thenReturn(comments)

        val result = commentsService.getAll()

        assertEquals(2, result.size)
    }

    @Test
    fun getComment_FlujoCompleto() {
        val id = UUID.randomUUID()
        val comment = buildComment(id = id, content = "Hola")
        whenever(commentRepository.findById(id)).thenReturn(Optional.of(comment))

        val result = commentsService.getComment(id)

        assertEquals("Hola", result.content)
    }

    @Test
    fun getComment_idInexistente() {
        val id = UUID.randomUUID()
        whenever(commentRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            commentsService.getComment(id)
        }
    }


}