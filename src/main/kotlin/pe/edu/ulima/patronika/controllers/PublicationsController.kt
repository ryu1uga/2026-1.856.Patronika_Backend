package pe.edu.ulima.patronika.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Publication
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.services.PublicationsService
import java.util.UUID

@RestController
@RequestMapping("/api/publications")
class PublicationsController (
    private val publicationsService: PublicationsService,
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

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postPublication(
        @RequestPart("publication") publicationRequest: PublicationRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<ApiResponse<Publication>> {
        val insertedPublication = publicationsService.insertPublication(publicationRequest, file)
        return ResponseEntity(ApiResponse(true, insertedPublication), HttpStatus.CREATED)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun putPublication(
        @PathVariable id: UUID,
        @RequestPart("publication") publicationRequest: PublicationRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ) : ResponseEntity<ApiResponse<String>> {
        publicationsService.updatePublication(id, publicationRequest, file)
        return ResponseEntity.ok(ApiResponse(true, "Publicación modificada exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deletePublication(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        publicationsService.deletePublication(id)
        return ResponseEntity.ok(ApiResponse(true, "Publicación eliminada exitosamente"))
    }
}