package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PatternCreateRequest
import pe.edu.ulima.patronika.dto.PatternRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PatternsServiceTest {
    @Mock
    private lateinit var patternRepository: PatternRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var imageConvolutionService: ImageConvolutionService

    @InjectMocks
    private lateinit var patternsService: PatternsService

    private fun buildUser(id: UUID = UUID.randomUUID()) =
        User(id = id, username = "user", email = "user@example.com", hashedPassword = "hash")

    private fun buildPattern(
        id: UUID? = UUID.randomUUID(),
        user: User = buildUser(),
        name: String = "Mi patron",
        width: Int = 10,
        height: Int = 10
    ) = Pattern(id = id, user = user, name = name, width = width, height = height)

    //InsertPattern
    @Test
    fun insertPattern_conImagen_devuelvePatronConGridData() {
        val userId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val request = PatternCreateRequest(name = "Mi patron", width = 10, height = 10)
        val file = MockMultipartFile("file", "foto.png", "image/png", byteArrayOf(1, 2, 3))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(imageConvolutionService.imageToGridData(file, 10, 10)).thenReturn("[[\"#FF0000\"]]")
        whenever(patternRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = patternsService.insertPattern(userId, request, file)

        assertEquals("Mi patron", result.name)
        assertEquals("[[\"#FF0000\"]]", result.gridData)
        verify(imageConvolutionService).imageToGridData(file, 10, 10)
    }

    @Test
    fun insertPattern_sinImagen_gridDataNulo() {
        val userId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val request = PatternCreateRequest(name = "Mi patron", width = 10, height = 10)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = patternsService.insertPattern(userId, request, image = null)

        assertNull(result.gridData)
        verifyNoInteractions(imageConvolutionService)
    }

    @Test
    fun insertPattern_archivoVacio_gridDataNulo() {
        val userId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val request = PatternCreateRequest(name = "Mi patron", width = 10, height = 10)
        val archivoVacio = MockMultipartFile("file", "vacio.png", "image/png", ByteArray(0))

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = patternsService.insertPattern(userId, request, archivoVacio)

        assertNull(result.gridData)
        verifyNoInteractions(imageConvolutionService)
    }

    @Test
    fun insertPattern_usuarioInexistente() {
        val userId = UUID.randomUUID()
        val request = PatternCreateRequest(name = "Mi patron", width = 10, height = 10)

        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        val error = assertThrows(BadRequestException::class.java) {
            patternsService.insertPattern(userId, request, image = null)
        }
        assertEquals("Usuario no registrado", error.message)
        verify(patternRepository, never()).save(any())
    }



    //Update Pattern
    @Test
    fun updatePattern_FlujoCompleto() {
        val patternId = UUID.randomUUID()
        val pattern = buildPattern(id = patternId, name = "Viejo", width = 5, height = 5)
        val req = PatternRequest(name = "Nuevo", width = 20, height = 20)

        whenever(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern))

        patternsService.updatePattern(patternId, req)

        assertEquals("Nuevo", pattern.name)
        assertEquals(20, pattern.width)
        assertEquals(20, pattern.height)
        verify(patternRepository).save(pattern)
    }

    @Test
    fun updatePattern_idInexistente() {
        val patternId = UUID.randomUUID()
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            patternsService.updatePattern(patternId, PatternRequest(name = "X", width = 1, height = 1))
        }
    }

    //Delete Pattern
    @Test
    fun deletePattern_existente() {
        val patternId = UUID.randomUUID()
        whenever(patternRepository.existsById(patternId)).thenReturn(true)

        patternsService.deletePattern(patternId)

        verify(patternRepository).deleteById(patternId)
    }

    @Test
    fun deletePattern_inexistente() {
        val patternId = UUID.randomUUID()
        whenever(patternRepository.existsById(patternId)).thenReturn(false)

        assertThrows(NotFoundException::class.java) {
            patternsService.deletePattern(patternId)
        }
        verify(patternRepository, never()).deleteById(any())
    }



    //GetAllUsersByID
    @Test
    fun getAllByUserId_usuarioExiste() {
        val userId = UUID.randomUUID()
        val patterns = listOf(buildPattern(), buildPattern())

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(patterns)

        val result = patternsService.getAllByUserId(userId)

        assertEquals(2, result.size)
    }

    @Test
    fun getAllByUserId_usuarioInexistente() {
        val userId = UUID.randomUUID()
        whenever(userRepository.existsById(userId)).thenReturn(false)

        val error = assertThrows(BadRequestException::class.java) {
            patternsService.getAllByUserId(userId)
        }
        assertEquals("Usuario no registrado", error.message)
    }

    //get pattern
    @Test
    fun getPattern_idExistente() {
        val patternId = UUID.randomUUID()
        val pattern = buildPattern(id = patternId)
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern))

        val result = patternsService.getPattern(patternId)

        assertEquals(pattern.name, result.name)
    }

    @Test
    fun getPattern_idInexistente_lanzaNotFoundException() {
        val patternId = UUID.randomUUID()
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            patternsService.getPattern(patternId)
        }
    }

    @Test
    fun getPatternsOfUser_usuarioExiste_flujoCompleto() {
        val userId = UUID.randomUUID()
        val patterns = listOf(buildPattern(), buildPattern())

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(patterns)

        val result = patternsService.getPatternsOfUser(userId)

        assertEquals(2, result.size)
        assertEquals(patterns, result)
    }

    @Test
    fun getPatternsOfUser_usuarioInexistente() {
        val userId = UUID.randomUUID()
        whenever(userRepository.existsById(userId)).thenReturn(false)

        assertThrows(BadRequestException::class.java) {
            patternsService.getPatternsOfUser(userId)
        }
    }

    @Test
    fun getAll_devuelveListaDePatronesMapeadaADto() {
        val user = buildUser()
        val patterns = listOf(
            buildPattern(user = user, name = "Patron 1"),
            buildPattern(user = user, name = "Patron 2")
        )

        whenever(patternRepository.findAllByOrderByCreatedAtDesc()).thenReturn(patterns)

        val result = patternsService.getAll()

        assertEquals(2, result.size)
        assertEquals("Patron 1", result[0].name)
        assertEquals("Patron 2", result[1].name)
    }



}