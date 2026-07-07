package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.PublicationRepository
import pe.edu.ulima.patronika.database.repository.PublishedPatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PublicationsServiceTest {
    @Mock
    private lateinit var publicationRepository: PublicationRepository
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var patternRepository: PatternRepository
    @Mock
    private lateinit var cloudinaryService: CloudinaryService
    @Mock
    private lateinit var publishedPatternRepository: PublishedPatternRepository
    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var publicationsService: PublicationsService

    private fun buildUser(
        id: UUID = UUID.randomUUID(),
        isAdmin: Boolean = false
    ) = User(
        id = id,
        username = "user",
        email = "user@example.com",
        hashedPassword = "hash",
        isAdmin = isAdmin
    )

    private fun buildPattern(
        id: UUID = UUID.randomUUID(),
        user: User = buildUser()) =
        Pattern(
            id = id,
            user = user,
            name = "Patron"
        )

    private fun buildPublication(
        id: UUID? = UUID.randomUUID(),
        user: User = buildUser(),
        pattern: Pattern = buildPattern(),
        description: String = "Descripcion",
        imageUrl: String? = null,
        reportCount: Int = 0
    ) = Publication(
        id = id,
        user = user,
        pattern = pattern,
        description = description,
        imageUrl = imageUrl,
        reportCount = reportCount
    )


    //insertPublication
    @Test
    fun insertPublication_FlujoCompleto() {
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val pattern = buildPattern(id = patternId, user = user)
        val request = PublicationRequest(userId = userId, patternId = patternId, description = "Bufanda")

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern))
        whenever(publicationRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(publishedPatternRepository.existsByUserIdAndPatternId(userId, patternId)).thenReturn(false)

        val result = publicationsService.insertPublication(request, file = null)

        assertEquals("Bufanda", result.description)
        verifyNoInteractions(cloudinaryService)
        verify(publishedPatternRepository).save(any())
    }


    @Test
    fun insertPublication_FlujoConImagen() {
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val pattern = buildPattern(id = patternId, user = user)
        val request = PublicationRequest(userId = userId, patternId = patternId)
        val file = MockMultipartFile("file", "foto.png", "image/png", byteArrayOf(1, 2, 3))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern))
        whenever(cloudinaryService.uploadImage(file, folder = "patterns")).thenReturn("https://cloudinary.com/foto.png")
        whenever(publicationRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(publishedPatternRepository.existsByUserIdAndPatternId(userId, patternId)).thenReturn(false)

        val result = publicationsService.insertPublication(request, file)

        assertEquals("https://cloudinary.com/foto.png", result.imageUrl)
        verify(cloudinaryService).uploadImage(file, folder = "patterns")
    }



    @Test
    fun insertPublication_UsuarioNoRegistrado() {
        val req = PublicationRequest(userId = UUID.randomUUID(), patternId = UUID.randomUUID())
        whenever(userRepository.findById(req.userId)).thenReturn(Optional.empty())

        val error = assertThrows(BadRequestException::class.java) {
            publicationsService.insertPublication(req, file = null)
        }
        assertEquals("Usuario no registrado", error.message)
    }

    @Test
    fun insertPublication_PatronNoRegistrado() {
        val userId = UUID.randomUUID()
        val req = PublicationRequest(userId = userId, patternId = UUID.randomUUID())
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(id = userId)))
        whenever(patternRepository.findById(req.patternId)).thenReturn(Optional.empty())

        val error = assertThrows(BadRequestException::class.java) {
            publicationsService.insertPublication(req, file = null)
        }
        assertEquals("Patrón no registrado", error.message)
    }

    //updatePublication

    @Test
    fun updatePublication_FlujoCompleto() {
        val id = UUID.randomUUID()
        val publication = buildPublication(id = id, description = "Vieja", imageUrl = "https://cloudinary.com/old.png")
        val req = PublicationRequest(userId = UUID.randomUUID(), patternId = UUID.randomUUID(), description = "Nueva", technique = 1)

        whenever(publicationRepository.findById(id)).thenReturn(Optional.of(publication))

        publicationsService.updatePublication(id, req, file = null)

        assertEquals("Nueva", publication.description)
        assertEquals(1, publication.technique)
        assertEquals("https://cloudinary.com/old.png", publication.imageUrl)
        verifyNoInteractions(cloudinaryService)
        verify(publicationRepository).save(publication)
    }

    @Test
    fun updatePublication_ConImagenNueva() {
        val id = UUID.randomUUID()
        val publication = buildPublication(id = id, imageUrl = "https://cloudinary.com/old.png")
        val req = PublicationRequest(userId = UUID.randomUUID(), patternId = UUID.randomUUID())
        val file = MockMultipartFile("file", "nueva.png", "image/png", byteArrayOf(9))

        whenever(publicationRepository.findById(id)).thenReturn(Optional.of(publication))
        whenever(cloudinaryService.uploadImage(file, folder = "patterns")).thenReturn("https://cloudinary.com/new.png")

        publicationsService.updatePublication(id, req, file)

        verify(cloudinaryService).deleteImage("https://cloudinary.com/old.png")
        verify(cloudinaryService).uploadImage(file, folder = "patterns")
        assertEquals("https://cloudinary.com/new.png", publication.imageUrl)
    }

    //deletePublication
    @Test
    fun deletePublication_FlujoCompleto() {
        val id = UUID.randomUUID()
        val publication = buildPublication(id = id)

        whenever(publicationRepository.findById(id)).thenReturn(Optional.of(publication))

        publicationsService.deletePublication(id)

        verify(publicationRepository).delete(publication)
    }

    //adminDeletePublication

    @Test
    fun adminDeletePublication_FlujoCompleto() {
        val adminId = UUID.randomUUID()
        val publicationId = UUID.randomUUID()
        val admin = buildUser(id = adminId, isAdmin = true)
        val dueño = buildUser()
        val publication = buildPublication(id = publicationId, user = dueño)

        whenever(userRepository.findById(adminId)).thenReturn(Optional.of(admin))
        whenever(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication))

        publicationsService.adminDeletePublication(publicationId, adminId, reason = "Contenido inapropiado")

        verify(publicationRepository).delete(publication)
        verify(emailService).sendPublicationDeletedEmail(
            dueño.email,
            dueño.username,
            "Contenido inapropiado"
        )
    }

    @Test
    fun adminDeletePublication_NoEncuentraAdmin(){
        val adminId = UUID.randomUUID()
        val publicationId = UUID.randomUUID()

        whenever(userRepository.findById(adminId)).thenReturn(Optional.empty())

        val error = assertThrows(BadRequestException::class.java) {
            publicationsService.adminDeletePublication(publicationId, adminId, reason = "Contenido inapropiado")
        }
        assertEquals("Admin no encontrado", error.message)

    }

    @Test
    fun adminDeletePublication_NoEsAdmin(){
        val fakeAdminId = UUID.randomUUID()
        val publicationId = UUID.randomUUID()
        val fakeAdmin = buildUser(id = fakeAdminId, isAdmin = false)

        whenever(userRepository.findById(fakeAdminId)).thenReturn(Optional.of(fakeAdmin))

       assertThrows(UnauthorizedException::class.java) {
            publicationsService.adminDeletePublication(publicationId, fakeAdminId, reason = "Contenido inapropiado")
       }

    }

    //reportPublication
    @Test
    fun reportPublication_FlujoCompleto(){
        val publicationId = UUID.randomUUID()
        val publication = buildPublication(id = publicationId, reportCount = 0)
        whenever(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication))

        publicationsService.reportPublication(publicationId)

        assertEquals(1,publication.reportCount)
        verify(publicationRepository).save(publication)

    }

    //clearReports
    @Test
    fun clearReports_FlujoCompleto(){
        val publicationId = UUID.randomUUID()
        val publication = buildPublication(id = publicationId, reportCount = 5)
        whenever(publicationRepository.findById(publicationId)).thenReturn(Optional.of(publication))

        publicationsService.clearReports(publicationId)

        assertEquals(0,publication.reportCount)
        verify(publicationRepository).save(publication)

    }

    //getAll
    @Test
    fun getAll_FlujoCompleto(){
        val publications = listOf(buildPublication(), buildPublication())
        whenever(publicationRepository.findAllByOrderByPublishedAtDesc()).thenReturn(publications)

        val result = publicationsService.getAll()

        assertEquals(2, result.size)
    }

    //getPublication
    @Test
    fun getPublication_FlujoCompleto(){
        val id = UUID.randomUUID()
        val publication = buildPublication(id = id, description = "Mi publicacion")
        whenever(publicationRepository.findById(id)).thenReturn(Optional.of(publication))

        val result = publicationsService.getPublication(id)

        assertEquals("Mi publicacion", result.description)
    }

    @Test
    fun getPublication_NoEncuentraPublicacion() {
        val id = UUID.randomUUID()
        whenever(publicationRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            publicationsService.getPublication(id)
        }
    }





}