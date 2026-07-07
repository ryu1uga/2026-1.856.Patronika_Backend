package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.PatternLibrary
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternLibraryRepository
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PatternLibraryRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PatternLibraryServiceTest {
    @Mock
    private lateinit var patternLibraryRepository: PatternLibraryRepository

    @Mock
    private lateinit var patternRepository: PatternRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var patternLibraryService: PatternLibraryService

    private fun buildUser(
        id: UUID = UUID.randomUUID()
        ) = User (
            id = id,
            username = "user",
            email = "user@example.com",
            hashedPassword = "hash"
        )
    private fun buildPattern(
        id: UUID? = UUID.randomUUID(),
        user: User = buildUser(),
        name: String = "Patron",
        createdAt: Instant = Instant.now()
    ) = Pattern(
        id = id,
        user = user,
        name = name,
        createdAt = createdAt
    )

    private fun buildPatternLibrary(
        id: UUID? = UUID.randomUUID(),
        user: User,
        pattern: Pattern,
        savedAt: Instant = Instant.now()
    ) = PatternLibrary(
        id = id,
        user = user,
        pattern = pattern,
        savedAt = savedAt
    )

    //savePattern
    @Test
    fun savePattern_FlujoCompleto(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val pattern = buildPattern(id = patternId,user = user)
        val req = PatternLibraryRequest(userId = userId, patternId = patternId)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern))
        whenever(patternLibraryRepository.existsByUserIdAndPatternId(userId,patternId)).thenReturn(false)
        whenever(patternLibraryRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = patternLibraryService.savePattern(req)

        assertEquals(pattern.name, result.pattern.name)
        verify(patternLibraryRepository).save(any())
    }

    @Test
    fun savePattern_UsuarioNoRegistrado(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()
        val req = PatternLibraryRequest(userId = userId, patternId = patternId)
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())
        val error = assertThrows(BadRequestException::class.java) {
            patternLibraryService.savePattern(req)
        }
        assertEquals("Usuario no registrado", error.message)
        verify(patternRepository, never()).findById(any())

    }

    @Test
    fun savePattern_PatronNoRegistrado(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val req = PatternLibraryRequest(userId = userId, patternId = patternId)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.empty())
        val error = assertThrows(BadRequestException::class.java) {
            patternLibraryService.savePattern(req)
        }
        assertEquals("Patrón no registrado", error.message)

    }

    @Test
    fun savePattern_PatronYaRegistrado(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val pattern = buildPattern(id = patternId,user = user)
        val req = PatternLibraryRequest(userId = userId, patternId = patternId)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(patternRepository.findById(patternId)).thenReturn(Optional.of(pattern))
        whenever(patternLibraryRepository.existsByUserIdAndPatternId(userId,patternId)).thenReturn(true)

        val error = assertThrows(ConflictException::class.java) {
            patternLibraryService.savePattern(req)
        }
        assertEquals("El patrón ya está en la biblioteca del usuario", error.message)

    }

    //removePatttern
    @Test
    fun removePattern_FlujoCompleto(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternRepository.existsById(patternId)).thenReturn(true)
        whenever(patternLibraryRepository.existsByUserIdAndPatternId(userId,patternId)).thenReturn(true)

        patternLibraryService.removePattern(userId, patternId)

        verify(patternLibraryRepository).deleteByUserIdAndPatternId(userId,patternId)
    }

    @Test
    fun removePattern_UsuarioNoRegistrado(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()

        whenever(userRepository.existsById(userId)).thenReturn(false)

        val error = assertThrows(BadRequestException::class.java) {
            patternLibraryService.removePattern(userId, patternId)
        }
        assertEquals("Usuario no registrado", error.message)
    }

    @Test
    fun removePattern_PatronNoRegistrado(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternRepository.existsById(patternId)).thenReturn(false)

        val error = assertThrows(BadRequestException::class.java) {
            patternLibraryService.removePattern(userId, patternId)
        }
        assertEquals("Patrón no registrado", error.message)
    }

    @Test
    fun removePattern_VinculoNoEncontrado(){
        val userId = UUID.randomUUID()
        val patternId = UUID.randomUUID()

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternRepository.existsById(patternId)).thenReturn(true)
        whenever(patternLibraryRepository.existsByUserIdAndPatternId(userId,patternId)).thenReturn(false)

        assertThrows(NotFoundException::class.java) {
            patternLibraryService.removePattern(userId, patternId)
        }
    }

    //getLibraryByUser
    @Test
    fun getLibraryByUser_FlujoCompleto(){
        val userId = UUID.randomUUID()
        val user = buildUser(id = userId)
        val pattern = buildPattern(user = buildUser())
        val entries = listOf(buildPatternLibrary(user = user, pattern = pattern))

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternLibraryRepository.findAllByUserIdOrderBySavedAtDesc(userId)).thenReturn(entries)

        val result = patternLibraryService.getLibraryByUser(userId)
        assertEquals(1, result.size)

    }

    @Test
    fun getLibraryByUser_UsuarioNoRegistrado(){
        val userId = UUID.randomUUID()

        whenever(userRepository.existsById(userId)).thenReturn(false)

        val error = assertThrows(BadRequestException::class.java) {
            patternLibraryService.getLibraryByUser(userId)
        }
        assertEquals("Usuario no registrado", error.message)
    }

    //getAllPatternsOfUser
    @Test
    fun getAllPatternsOfUser_FlujoCompleto(){
        val userId = UUID.randomUUID()
        val otroUsuario = buildUser()

        val patronPropio = buildPattern(
            user = buildUser(id = userId),
            name = "Propio",
            createdAt = Instant.now().minusSeconds(3600)
        )
        val patronGuardado = buildPattern(
            user = otroUsuario,
            name = "Guardado",
            createdAt = Instant.now()
        )
        val entradaBiblioteca = buildPatternLibrary(
            user = buildUser(id = userId),
            pattern = patronGuardado
        )

        whenever(userRepository.existsById(userId)).thenReturn(true)
        whenever(patternRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(listOf(patronPropio))
        whenever(patternLibraryRepository.findAllByUserIdOrderBySavedAtDesc(userId)).thenReturn(listOf(entradaBiblioteca))

        val result = patternLibraryService.getAllPatternsOfUser(userId)

        assertEquals(2, result.size)
        assertEquals("Guardado", result[0].name)
        assertEquals("Propio", result[1].name)

    }

    @Test
    fun getAllPatternsOfUser_UsuarioNoRegistrado(){
        val userId = UUID.randomUUID()
        whenever(userRepository.existsById(userId)).thenReturn(false)
        val error = assertThrows(BadRequestException::class.java) {
            patternLibraryService.getAllPatternsOfUser(userId)
        }
        assertEquals("Usuario no registrado", error.message)

    }



}