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
import pe.edu.ulima.patronika.database.repository.TutorialRepository
import pe.edu.ulima.patronika.dto.TutorialRequest
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TutorialsServiceTest {
    @Mock
    private lateinit var tutorialRepository: TutorialRepository

    @InjectMocks
    private lateinit var tutorialsService: TutorialsService

    private fun buildTutorial(
        id: UUID? = UUID.randomUUID(),
        title: String = "Title",
        description: String = "Description",
        url: String = "https://youtube.com/video1"
    ) = Tutorial(
        id = id,
        title = title,
        description = description,
        url = url
    )

    //insertTutorial
    @Test
    fun insertTutorial_FlujoCompleto() {
        val req = TutorialRequest(title = "Punto alto", description = "Desc", url = "https://youtube.com/x")

        whenever(tutorialRepository.save(any())).thenAnswer { it.arguments[0] }
        val result = tutorialsService.insertTutorial(req)

        assertEquals("Punto alto", result.title)
        assertEquals("Desc", result.description)
        assertEquals("https://youtube.com/x", result.url)
        verify(tutorialRepository).save(any())
    }

    @Test
    fun updateTutorial_FlujoCompleto() {
        val id = UUID.randomUUID()
        val tutorialCambio = buildTutorial(id , title = "Viejo", description = "Descripcion vieja", url = "https://old.com")
        val req = TutorialRequest(title = "Punto alto", description = "Descripcion nueva", url = "https://youtube.com/x")

        whenever(tutorialRepository.findById(id)).thenReturn(Optional.of(tutorialCambio))

        tutorialsService.updateTutorial(id, req)

        assertEquals("Punto alto", tutorialCambio.title)
        assertEquals("Descripcion nueva", tutorialCambio.description)
        assertEquals("https://youtube.com/x", tutorialCambio.url)
        verify(tutorialRepository).save(tutorialCambio)
    }

    //deleteTutorial
    @Test
    fun deleteTutorial_FlujoCompleto() {
        val id = UUID.randomUUID()

        whenever(tutorialRepository.existsById(id)).thenReturn(true)

        tutorialsService.deleteTutorial(id)
        verify(tutorialRepository).deleteById(id)

    }

    @Test
    fun deleteTutorial_TutorialNoExiste() {
        val id = UUID.randomUUID()

        whenever(tutorialRepository.existsById(id)).thenReturn(false)

        assertThrows(NotFoundException::class.java) {
            tutorialsService.deleteTutorial(id)
        }
        verify(tutorialRepository, never()).deleteById(any())


    }

    //getAll
    @Test
    fun getAll_FlujoCompleto() {
        val lista = listOf(buildTutorial(), buildTutorial())
        whenever(tutorialRepository.findAll()).thenReturn(lista)

        assertEquals(lista, tutorialsService.getAll())
    }

    //getTutorial
    @Test
    fun getTutorial_FlujoCompleto() {
        val id = UUID.randomUUID()
        val tutorial = buildTutorial(id = id)
        whenever(tutorialRepository.findById(id)).thenReturn(Optional.of(tutorial))

        assertEquals(tutorial, tutorialsService.getTutorial(id))
    }

    @Test
    fun getTutorial_NoExisteTutorial() {
        val id = UUID.randomUUID()
        whenever(tutorialRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            tutorialsService.getTutorial(id)
        }
    }

}