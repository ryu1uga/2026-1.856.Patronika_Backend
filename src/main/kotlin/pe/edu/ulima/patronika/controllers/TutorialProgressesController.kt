package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.TutorialProgress
import pe.edu.ulima.patronika.dto.TutorialProgressRequest
import pe.edu.ulima.patronika.services.TutorialProgressesService
import java.util.UUID

@RestController
@RequestMapping("/api/tutorialProgresses")
class TutorialProgressesController (
    private val tutorialProgressesService: TutorialProgressesService
) {
    @GetMapping
    fun loadAllTutorialProgresses(): ResponseEntity<ApiResponse<List<TutorialProgress>>> {
        val tutorialProgresses = tutorialProgressesService.getAll()
        return ResponseEntity.ok(ApiResponse(true, tutorialProgresses))
    }

    @GetMapping("/{id}")
    fun loadTutorialProgress(@PathVariable id: UUID): ResponseEntity<ApiResponse<TutorialProgress>> {
        val tutorialProgress = tutorialProgressesService.getTutorialProgress(id)
        return ResponseEntity.ok(ApiResponse(true, tutorialProgress))
    }

    @PostMapping
    fun postTutorialProgress(
        @RequestHeader("UserId") userId: UUID,
        @Valid @RequestBody tutorialProgressRequest: TutorialProgressRequest
    ): ResponseEntity<ApiResponse<TutorialProgress>> {
        val insertedTutorialProgress = tutorialProgressesService.insertTutorialProgress(userId, tutorialProgressRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedTutorialProgress))
    }

    @PutMapping("/{id}")
    fun putTutorialProgress(
        @RequestHeader("Id") id: UUID,
        @Valid @RequestBody tutorialProgressRequest: TutorialProgressRequest
    ) : ResponseEntity<ApiResponse<String>> {
        tutorialProgressesService.updateTutorialProgress(id, tutorialProgressRequest)
        return ResponseEntity.ok(ApiResponse(true, "Progreso de tutorial modificado exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deleteTutorialProgress(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        tutorialProgressesService.deleteTutorialProgress(id)
        return ResponseEntity.ok(ApiResponse(true, "Progreso de tutorial eliminado exitosamente"))
    }
}