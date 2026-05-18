package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.TutorialProgress
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.TutorialProgressRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.TutorialProgressRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.time.LocalDate
import java.util.UUID

@Service
class TutorialProgressesService (
    private val tutorialProgressRepository: TutorialProgressRepository,
    private val userRepository: UserRepository
) {
    fun getAll(): List<TutorialProgress> = tutorialProgressRepository.findAll()

    fun getTutorialProgress(id: UUID): TutorialProgress {
        return tutorialProgressRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    fun insertTutorialProgress(
        userId: UUID,
        tutorialProgressRequest: TutorialProgressRequest
    ): TutorialProgress {
        val user = getUser(userId)

        val tutorialProgress = TutorialProgress(
            user = user,
            status = tutorialProgressRequest.status
        )

        return tutorialProgressRepository.save(tutorialProgress)
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