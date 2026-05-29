package pe.edu.ulima.patronika.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.services.PublicationsService
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@RestController
@RequestMapping("/api/publications")
class PublicationsController (
    private val publicationsService: PublicationsService,
    private val objectMapper: ObjectMapper
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

    @PostMapping(consumes = ["multipart/form-data"])
    fun postPublication(
        @RequestParam userId: UUID,
        @RequestParam patternId: UUID,
        @RequestParam("publication") publicationJson: String,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<ApiResponse<Publication>> {
        val publicationRequest = objectMapper.readValue(publicationJson, PublicationRequest::class.java)
        val insertedPublication = publicationsService.insertPublication(userId, patternId, publicationRequest, file)
        return ResponseEntity(ApiResponse(true, insertedPublication), HttpStatus.CREATED)
    }

    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun putPublication(
        @PathVariable id: UUID,
        @RequestParam("publication") publicationJson: String,
        @RequestPart("file", required = false) file: MultipartFile?
    ) : ResponseEntity<ApiResponse<String>> {
        val publicationRequest = objectMapper.readValue(publicationJson, PublicationRequest::class.java)
        publicationsService.updatePublication(id, publicationRequest, file)
        return ResponseEntity.ok(ApiResponse(true, "Publicación modificada exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deletePublication(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        publicationsService.deletePublication(id)
        return ResponseEntity.ok(ApiResponse(true, "Publicación eliminada exitosamente"))
    }
}