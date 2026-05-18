package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.services.PublicationsService
import java.util.UUID

@RestController
@RequestMapping("/api/publications")
class PublicationsController (
    private val publicationsService: PublicationsService
) {
    @GetMapping
    fun loadAllPublications(): ResponseEntity<ApiResponse<List<Publication>>> {
        val publications = publicationsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, publications))
    }

    @GetMapping("/{id}")
    fun loadPublication(@PathVariable id: UUID): ResponseEntity<ApiResponse<Publication>> {
        val publication = publicationsService.getPublication(id)
        return ResponseEntity.ok(ApiResponse(true, publication))
    }

    @PostMapping
    fun postPublication(
        @RequestHeader("userId") userId: UUID,
        @RequestHeader("patternId") patternId: UUID,
        @Valid @RequestBody publicationRequest: PublicationRequest
    ): ResponseEntity<ApiResponse<Publication>> {
        val insertedPublication = publicationsService.insertPublication(userId, patternId, publicationRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedPublication))
    }

    @PutMapping("/{id}")
    fun putPublication(
        @PathVariable id: UUID,
        @Valid @RequestBody publicationRequest: PublicationRequest
    ) : ResponseEntity<ApiResponse<String>> {
        publicationsService.updatePublication(id, publicationRequest)
        return ResponseEntity.ok(ApiResponse(true, "Publicación modificada exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deletePublication(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        publicationsService.deletePublication(id)
        return ResponseEntity.ok(ApiResponse(true, "Publicación eliminada exitosamente"))
    }
}