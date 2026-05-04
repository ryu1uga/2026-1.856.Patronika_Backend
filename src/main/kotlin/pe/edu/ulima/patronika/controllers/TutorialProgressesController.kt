package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
        val tutorialProgress = tutorialProgressesService.getAll()
        return ResponseEntity.ok(ApiResponse(true, tutorialProgress))
    }

    @GetMapping("/{id}")
    fun loadTutorialProgress(@PathVariable id: UUID): ResponseEntity<ApiResponse<TutorialProgress>> {
        val tutorialProgress = tutorialProgressesService.getTutorialProgress(id)
        return ResponseEntity.ok(ApiResponse(true, tutorialProgress))
    }

    @PostMapping
    fun postTutorialProgress(
        @Valid @RequestBody tutorialProgressRequest: TutorialProgressRequest
    ): ResponseEntity<ApiResponse<TutorialProgress>> {
        val insertedTutorialProgress = tutorialProgressesService
        return
    }
}