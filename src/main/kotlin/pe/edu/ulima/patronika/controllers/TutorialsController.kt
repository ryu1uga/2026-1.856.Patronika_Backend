package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Tutorial
import pe.edu.ulima.patronika.dto.TutorialRequest
import pe.edu.ulima.patronika.services.TutorialsService
import java.util.UUID

@RestController
@RequestMapping("/api/tutorials")
class TutorialsController (
    private val tutorialsService: TutorialsService
) {
    @GetMapping
    fun loadAllTutorials() : ResponseEntity<ApiResponse<List<Tutorial>>> {
        val tutorials = tutorialsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, tutorials))
    }

    @GetMapping("/{id}")
    fun loadTutorial(@PathVariable id : UUID) : ResponseEntity<ApiResponse<Tutorial>> {
        val tutorial = tutorialsService.getTutorial(id)
        return ResponseEntity.ok(ApiResponse(true, tutorial))
    }

    @PostMapping
    fun postTutorial(
        @Valid @RequestBody tutorialRequest: TutorialRequest
    ) : ResponseEntity<ApiResponse<Tutorial>> {
        val insertedTutorial = tutorialsService.insertTutorial(tutorialRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedTutorial))
    }

    @PutMapping("/{id}")
    fun putTutorial(
        @PathVariable id: UUID,
        @Valid @RequestBody tutorialRequest: TutorialRequest
    ) : ResponseEntity<ApiResponse<String>> {
        tutorialsService.updateTutorial(id, tutorialRequest)
        return ResponseEntity.ok(ApiResponse(true, "Tutorial modificado exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deleteTutorial(@PathVariable id : UUID) : ResponseEntity<ApiResponse<String>> {
        tutorialsService.deleteTutorial(id)
        return ResponseEntity.ok(ApiResponse(true, "Tutorial eliminado exitosamente"))
    }
}