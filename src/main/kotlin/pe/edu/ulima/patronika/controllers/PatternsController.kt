package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.dto.PatternRequest
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

    @GetMapping("/{id}")
    fun loadPattern(@PathVariable id: UUID): ResponseEntity<ApiResponse<Pattern>> {
        val pattern = patternsService.getPattern(id)
        return ResponseEntity.ok(ApiResponse(true, pattern))
    }

    @PostMapping
    fun postPattern(
        @RequestHeader("userId") userId: UUID,
        @Valid @RequestBody patternRequest: PatternRequest
    ): ResponseEntity<ApiResponse<Pattern>> {
        val insertedPattern = patternsService.insertPattern(userId, patternRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedPattern))
    }

    @PutMapping("/{id}")
    fun putPattern(
        @RequestHeader("id") id: UUID,
        @Valid @RequestBody patternRequest: PatternRequest
    ) : ResponseEntity<ApiResponse<String>> {
        patternsService.updatePattern(id, patternRequest)
        return ResponseEntity.ok(ApiResponse(true, "Patrón modificado exitosamente"))
    }

    @DeleteMapping("/{id}")
    fun deletePattern(@PathVariable id: UUID) : ResponseEntity<ApiResponse<String>> {
        patternsService.deletePattern(id)
        return ResponseEntity.ok(ApiResponse(true, "Patrón eliminado exitosamente"))
    }
}