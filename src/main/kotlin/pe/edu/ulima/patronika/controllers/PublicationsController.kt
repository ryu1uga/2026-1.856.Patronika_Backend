package pe.edu.ulima.patronika.controllers

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.dto.DeletePublicationRequest
import pe.edu.ulima.patronika.dto.PublicationRequest
import pe.edu.ulima.patronika.dto.PublicationResponseDto
import pe.edu.ulima.patronika.services.PublicationsService
import java.util.UUID

@RestController
@RequestMapping("/api/publications")
class PublicationsController (
    private val publicationsService: PublicationsService,
) {
    @GetMapping
    fun loadAllPublications(): ResponseEntity<ApiResponse<List<PublicationResponseDto>>> {
        val publications = publicationsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, publications))
    }

    @GetMapping("/{id}")
    fun loadPublication(@PathVariable id: UUID): ResponseEntity<ApiResponse<PublicationResponseDto>> {
        val publication = publicationsService.getPublication(id)
        return ResponseEntity.ok(ApiResponse(true, publication))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @SwaggerRequestBody(
        content = [Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            encoding = [Encoding(name = "publication", contentType = MediaType.APPLICATION_JSON_VALUE)]
        )]
    )
    fun postPublication(
        @RequestPart("publication") publicationRequest: PublicationRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<ApiResponse<PublicationResponseDto>> {
        val insertedPublication = publicationsService.insertPublication(publicationRequest, file)
        return ResponseEntity(ApiResponse(true, insertedPublication), HttpStatus.CREATED)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @SwaggerRequestBody(
        content = [Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            encoding = [Encoding(name = "publication", contentType = MediaType.APPLICATION_JSON_VALUE)]
        )]
    )
    fun putPublication(
        @PathVariable id: UUID,
        @RequestPart("publication") publicationRequest: PublicationRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ) : ResponseEntity<ApiResponse<String>> {
        publicationsService.updatePublication(id, publicationRequest, file)
        return ResponseEntity.ok(ApiResponse(true, "Publicación modificada exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deletePublication(@PathVariable id: UUID): ResponseEntity<ApiResponse<String>> {
        publicationsService.deletePublication(id)
        return ResponseEntity.ok(ApiResponse(true, "Publicación eliminada exitosamente"))
    }

    @DeleteMapping("/{id}/admin")
    fun adminDeletePublication(
        @PathVariable id: UUID,
        @RequestBody body: DeletePublicationRequest
    ): ResponseEntity<ApiResponse<String>> {
        publicationsService.adminDeletePublication(id, body.adminId, body.reason)
        return ResponseEntity.ok(ApiResponse(true, "Publicación eliminada exitosamente"))
    }

    @PostMapping("/{id}/report")
    fun reportPublication(@PathVariable id: UUID): ResponseEntity<ApiResponse<String>> {
        publicationsService.reportPublication(id)
        return ResponseEntity.ok(ApiResponse(true, "Publicación reportada"))
    }
}