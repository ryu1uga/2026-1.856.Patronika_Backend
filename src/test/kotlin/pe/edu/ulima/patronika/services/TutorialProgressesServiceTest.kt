package pe.edu.ulima.patronika.services

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pe.edu.ulima.patronika.database.model.Tutorial
import pe.edu.ulima.patronika.database.model.TutorialProgress
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.TutorialProgressRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.TutorialProgressRequest
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TutorialProgressesServiceTest {
    @Mock
    private lateinit var tutorialProgressRepository: TutorialProgressRepository
    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var tutorialProgressesService: TutorialProgressesService

    private fun buildUser(id: UUID = UUID.randomUUID()) =
        User(id = id, username = "user", email = "user@example.com", hashedPassword = "hash")

    private fun buildTutorialProgress(
        id: UUID? = UUID.randomUUID(),
        user: User = buildUser(),
        tutorial: Tutorial = Tutorial(),
        status: Int = 0
    ) = TutorialProgress(
        id = id,
        user = user,
        tutorial = tutorial,
        status = status
    )

    //insetTutorialProgress
    @Test
    fun insertTutorialProgresses_FlujoCompleto() {
        val userId = UUID.randomUUID()
        val user = buildUser(userId)
        val req = TutorialProgressRequest(status = 1)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(tutorialProgressRepository.save(any())).thenAnswer {it.arguments[0] }

        val result = tutorialProgressesService.insertTutorialProgress(userId, req)
        assertEquals(1,result.status)
        verify(tutorialProgressRepository).save(any())
    }

    //updateTutorialPrograss
    @Test
    fun updateTutorialProgresses_FlujoCompleto() {
        val id = UUID.randomUUID()
        val tutorialProgress = buildTutorialProgress(id)
        val req = TutorialProgressRequest(status = 5)

        whenever(tutorialProgressRepository.findById(id)).thenReturn(Optional.of(tutorialProgress))

        tutorialProgressesService.updateTutorialProgress(id,req)

        assertEquals(5,tutorialProgress.status)
        verify(tutorialProgressRepository).save(tutorialProgress)
    }

    //deleteTutorialProgress
    @Test
    fun deleteTutorialProgresses_FlujoCompleto() {
        val id = UUID.randomUUID()

        whenever(tutorialProgressRepository.existsById(id)).thenReturn(true)

        tutorialProgressesService.deleteTutorialProgress(id)
        verify(tutorialProgressRepository).deleteById(id)
    }

    @Test
    fun deleteTutorialProgresses_ProgresoDeTutorialNoExiste() {
        val id = UUID.randomUUID()

        whenever(tutorialProgressRepository.existsById(id)).thenReturn(false)
        assertThrows(NotFoundException::class.java){
            tutorialProgressesService.deleteTutorialProgress(id)
        }
        verify(tutorialProgressRepository, never()).deleteById(id)
    }

    //getAll

    @Test
    fun getAll_FlujoCompleto() {
        val lista = listOf(buildTutorialProgress(), buildTutorialProgress())
        whenever(tutorialProgressRepository.findAll()).thenReturn(lista)

        val result = tutorialProgressesService.getAll()
        assertEquals(lista.size, result.size)
    }

    //getTutorialPogress
    @Test
    fun getTutorialProgress_FlujoCompleto() {
        val id = UUID.randomUUID()
        val tutorialProgress = buildTutorialProgress(id, status = 1)
        whenever(tutorialProgressRepository.findById(id)).thenReturn(Optional.of(tutorialProgress))

        val result = tutorialProgressesService.getTutorialProgress(id)
        assertEquals(1, result.status)
    }

    @Test
    fun getTutorialProgress_ProgresoDeTutorialNoExiste() {
        val id = UUID.randomUUID()
        whenever(tutorialProgressRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java){
            tutorialProgressesService.getTutorialProgress(id)
        }
    }

}