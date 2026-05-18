package pe.edu.ulima.patronika.services;

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.Tutorial
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.TutorialRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.TutorialRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID


@Service
class TutorialsService (
    private val tutorialRepository: TutorialRepository
) {
    fun getAll(): List<Tutorial> = tutorialRepository.findAll()

    fun getTutorial(id: UUID): Tutorial {
        return tutorialRepository.findById(id).orElseThrow { NotFoundException() }
    }

    fun insertTutorial(
        tutorialRequest: TutorialRequest
    ): Tutorial {
        val tutorial = Tutorial(
            title = tutorialRequest.title,
            description = tutorialRequest.description,
            difficulty = tutorialRequest.difficulty,
            url = tutorialRequest.url
        )

        return tutorialRepository.save(tutorial)
    }

    fun updateTutorial(
        id: UUID,
        req: TutorialRequest
    ) {
        val tutorial = getTutorial(id)

        tutorial.title = req.title
        tutorial.description = req.description
        tutorial.difficulty = req.difficulty
        tutorial.url = req.url

        tutorialRepository.save(tutorial)
    }

    fun deleteTutorial(id: UUID) {
        if (!tutorialRepository.existsById(id)) throw NotFoundException()
        tutorialRepository.deleteById(id)
    }
}

