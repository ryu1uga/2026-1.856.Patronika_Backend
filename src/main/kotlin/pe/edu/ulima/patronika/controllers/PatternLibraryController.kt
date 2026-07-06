package pe.edu.ulima.patronika.controllers

import io.swagger.v3.oas.annotations.Operation

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.dto.PatternLibraryRequest
import pe.edu.ulima.patronika.dto.PatternLibraryResponse
import pe.edu.ulima.patronika.dto.PatternResponse
import pe.edu.ulima.patronika.services.PatternLibraryService
import java.util.UUID

@RestController
@RequestMapping("/api/pattern-library")
class PatternLibraryController(
    private val patternLibraryService: PatternLibraryService
) {
    // Guardar un patrón en la biblioteca del usuario
    @PostMapping
    @Operation(summary = "Save pattern to library")
    fun savePattern(
        @RequestBody request: PatternLibraryRequest
    ): ResponseEntity<ApiResponse<PatternLibraryResponse>> {
        val entry = patternLibraryService.savePattern(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(true, entry))
    }

    // Quitar un patrón de la biblioteca del usuario
    @DeleteMapping
    @Operation(summary = "Remove pattern from library")
    fun removePattern(
        @RequestBody request: PatternLibraryRequest
    ): ResponseEntity<ApiResponse<String>> {
        patternLibraryService.removePattern(request.userId, request.patternId)
        return ResponseEntity.ok(ApiResponse(true, "Patrón eliminado de la biblioteca"))
    }

    // Obtener la biblioteca de un usuario (solo los patrones guardados)
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user library")
    fun getLibrary(
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<List<PatternLibraryResponse>>> {
        val library = patternLibraryService.getLibraryByUser(userId)
        return ResponseEntity.ok(ApiResponse(true, library))
    }

    // Obtener todos los patrones de un usuario: propios + guardados en biblioteca
    @GetMapping("/user/{userId}/all")
    @Operation(summary = "Get user's own and saved patterns")
    fun getAllPatterns(
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<List<PatternResponse>>> {
        val patterns = patternLibraryService.getAllPatternsOfUser(userId)
        return ResponseEntity.ok(ApiResponse(true, patterns))
    }
}
