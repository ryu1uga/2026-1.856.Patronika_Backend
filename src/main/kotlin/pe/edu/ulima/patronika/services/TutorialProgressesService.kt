package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.TutorialProgress
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.TutorialProgressRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.TutorialProgressRequest
import pe.edu.ulima.patronika.dto.TutorialProgressResponse
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.time.LocalDate
import java.util.UUID

@Service
class TutorialProgressesService (
    private val tutorialProgressRepository: TutorialProgressRepository,
    private val userRepository: UserRepository
) {
    private fun TutorialProgress.toDto() = TutorialProgressResponse(
        id = id,
        userId = user.id,
        tutorialId = tutorial.id,
        status = status,
        registeredDate = registeredDate
    )

    fun getAll(): List<TutorialProgressResponse> =
        tutorialProgressRepository.findAll().map { it.toDto() }

    fun getTutorialProgress(id: UUID): TutorialProgressResponse {
        return tutorialProgressRepository.findById(id).orElseThrow { NotFoundException() }.toDto()
    }

    private fun getTutorialProgressEntity(id: UUID): TutorialProgress {
        return tutorialProgressRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    fun insertTutorialProgress(
        userId: UUID,
        tutorialProgressRequest: TutorialProgressRequest
    ): TutorialProgressResponse {
        val user = getUser(userId)

        val tutorialProgress = TutorialProgress(
            user = user,
            status = tutorialProgressRequest.status
        )

        return tutorialProgressRepository.save(tutorialProgress).toDto()
    }

    fun updateTutorialProgress(
        id: UUID,
        req: TutorialProgressRequest
    ) {
        val tutorialProgress = getTutorialProgressEntity(id)

        tutorialProgress.status = req.status

        tutorialProgressRepository.save(tutorialProgress)
    }

    fun deleteTutorialProgress(id: UUID) {
        if (!tutorialProgressRepository.existsById(id)) throw NotFoundException()
        tutorialProgressRepository.deleteById(id)
    }
}