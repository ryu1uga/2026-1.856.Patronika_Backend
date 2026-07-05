package pe.edu.ulima.patronika.controllers

import io.swagger.v3.oas.annotations.Operation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.repository.PublishedPatternRepository
import pe.edu.ulima.patronika.dto.PublishedPatternResponse
import java.util.UUID

@RestController
@RequestMapping("/api/published-patterns")
class PublishedPatternsController(
    private val publishedPatternRepository: PublishedPatternRepository
) {
    private fun pe.edu.ulima.patronika.database.model.PublishedPattern.toDto() = PublishedPatternResponse(
        id = id,
        userId = user.id,
        patternId = pattern.id,
        publishedAt = publishedAt
    )

    @GetMapping
    @Operation(summary = "List all published patterns")
    fun getAll(): ResponseEntity<ApiResponse<List<PublishedPatternResponse>>> {
        val result = publishedPatternRepository.findAll().map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, result))
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List published patterns by user")
    fun getByUser(@PathVariable userId: UUID): ResponseEntity<ApiResponse<List<PublishedPatternResponse>>> {
        val result = publishedPatternRepository.findAllByUserId(userId).map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, result))
    }

    @GetMapping("/pattern/{patternId}")
    @Operation(summary = "List published patterns by pattern")
    fun getByPattern(@PathVariable patternId: UUID): ResponseEntity<ApiResponse<List<PublishedPatternResponse>>> {
        val result = publishedPatternRepository.findAllByPatternId(patternId).map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, result))
    }
}
