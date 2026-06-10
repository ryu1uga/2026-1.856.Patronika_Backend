package pe.edu.ulima.patronika.controllers
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.dto.PatternCreateRequest
import pe.edu.ulima.patronika.dto.PatternRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.services.PatternsService
import java.util.UUID

@RestController
@RequestMapping("/api/patterns")
class PatternsController (
    private val patternsService: PatternsService
) {
    @GetMapping
    fun loadAllPatterns(): ResponseEntity<ApiResponse<List<Pattern>>> {
        val patterns = patternsService.getAll()
        return ResponseEntity.ok(ApiResponse(true, patterns))
    }

    @GetMapping("/user/{userId}")
    fun loadPatternsByUserId(@PathVariable userId: UUID): ResponseEntity<ApiResponse<List<Pattern>>> {
        val patterns = patternsService.getAllByUserId(userId)
        return ResponseEntity.ok(ApiResponse(true, patterns))
    }

    @GetMapping("/{id}")
    fun loadPattern(@PathVariable id: UUID): ResponseEntity<ApiResponse<Pattern>> {
        val pattern = patternsService.getPattern(id)
        return ResponseEntity.ok(ApiResponse(true, pattern))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createPattern(
        @RequestHeader("UserId") userId: UUID,
        @RequestPart("name") name: String,
        @RequestPart("width") width: String,
        @RequestPart("height") height: String,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<ApiResponse<Pattern>> {
        val widthInt = width.toIntOrNull()
            ?: throw BadRequestException("El width debe ser un número entero")
        val heightInt = height.toIntOrNull()
            ?: throw BadRequestException("El height debe ser un número entero")

        if (widthInt < 1) throw BadRequestException("El width debe ser mayor a 0")
        if (heightInt < 1) throw BadRequestException("El height debe ser mayor a 0")

        val request = PatternCreateRequest(name = name, width = widthInt, height = heightInt, image = image)
        val insertedPattern = patternsService.insertPattern(userId, request)
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