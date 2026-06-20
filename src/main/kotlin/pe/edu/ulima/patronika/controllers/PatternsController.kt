package pe.edu.ulima.patronika.controllers
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.dto.PatternCreateRequest
import pe.edu.ulima.patronika.dto.PatternRequest
import pe.edu.ulima.patronika.dto.PatternResponseDto
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.services.PatternsService
import java.util.UUID

@RestController
@RequestMapping("/api/patterns")
class PatternsController (
    private val patternsService: PatternsService
) {
    @GetMapping
    fun loadAllPatterns(): ResponseEntity<ApiResponse<List<PatternResponseDto>>> {
        val patterns = patternsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, patterns))
    }

    @GetMapping("/user/{userId}")
    fun loadPatternsByUserId(@PathVariable userId: UUID): ResponseEntity<ApiResponse<List<PatternResponseDto>>> {
        val patterns = patternsService.getAllByUserId(userId)
        return ResponseEntity.ok(ApiResponse(true, patterns))
    }

    @GetMapping("/{id}")
    fun loadPattern(@PathVariable id: UUID): ResponseEntity<ApiResponse<PatternResponseDto>> {
        val pattern = patternsService.getPattern(id)
        return ResponseEntity.ok(ApiResponse(true, pattern))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @SwaggerRequestBody(
        content = [Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            encoding = [Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)]
        )]
    )
    fun createPattern(
        @RequestHeader("UserId") userId: UUID,
        @RequestPart("request") request: PatternCreateRequest,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ApiResponse<PatternResponseDto>> {
        if (request.width < 1) throw BadRequestException("El width debe ser mayor a 0")
        if (request.height < 1) throw BadRequestException("El height debe ser mayor a 0")

        val insertedPattern = patternsService.insertPattern(userId, request, image)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedPattern))
    }

    @PutMapping("/{id}")
    fun updatePattern(
        @PathVariable id: UUID,
        @Valid @RequestBody patternRequest: PatternRequest
    ): ResponseEntity<ApiResponse<String>> {
        patternsService.updatePattern(id, patternRequest)
        return ResponseEntity.ok(ApiResponse(true, "Patrón modifiado exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deletePattern(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        patternsService.deletePattern(id)
        return ResponseEntity.ok(ApiResponse(true, "Patrón eliminado exitosamente"))
    }
}