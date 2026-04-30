package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.TutorialProgress
import pe.edu.ulima.patronika.database.repository.TutorialProgressRepository
import pe.edu.ulima.patronika.dto.TutorialProgressRequest
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID

@Service
class TutorialProgressesService (
    private val tutorialProgressRepository: TutorialProgressRepository,
) {
    fun getAll(): List<TutorialProgress> = tutorialProgressRepository.findAll()

    fun getTutorialProgress(id: UUID): TutorialProgress {
        return tutorialProgressRepository.findById(id).orElseThrow { NotFoundException() }
    }

    fun updateTutorialProgress(
        id: UUID,
        req: TutorialProgressRequest
    ) {
        val tutorialProgress = getTutorialProgress(id)

        tutorialProgress.status = req.status

        tutorialProgressRepository.save(tutorialProgress)
    }

    fun deleteTutorialProgress(id: UUID) {
        if (!tutorialProgressRepository.existsById(id)) throw NotFoundException()
        tutorialProgressRepository.deleteById(id)
    }
}