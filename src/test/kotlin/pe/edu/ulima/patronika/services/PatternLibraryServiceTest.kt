package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.PatternLibrary
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternLibraryRepository
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PatternLibraryRequest
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




}