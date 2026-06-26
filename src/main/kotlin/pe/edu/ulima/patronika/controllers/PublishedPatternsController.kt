package pe.edu.ulima.patronika.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.repository.PublishedPatternRepository
import pe.edu.ulima.patronika.dto.PublishedPatternResponseDto
import java.util.UUID

@RestController
@RequestMapping("/api/published-patterns")
class PublishedPatternsController(
    private val publishedPatternRepository: PublishedPatternRepository
) {
    private fun pe.edu.ulima.patronika.database.model.PublishedPattern.toDto() = PublishedPatternResponseDto(
        id = id,
        userId = user.id,
        patternId = pattern.id,
        publishedAt = publishedAt
    )

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<PublishedPatternResponseDto>>> {
        val result = publishedPatternRepository.findAll().map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, result))
    }

    @GetMapping("/user/{userId}")
    fun getByUser(@PathVariable userId: UUID): ResponseEntity<ApiResponse<List<PublishedPatternResponseDto>>> {
        val result = publishedPatternRepository.findAllByUserId(userId).map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, result))
    }

    @GetMapping("/pattern/{patternId}")
    fun getByPattern(@PathVariable patternId: UUID): ResponseEntity<ApiResponse<List<PublishedPatternResponseDto>>> {
        val result = publishedPatternRepository.findAllByPatternId(patternId).map { it.toDto() }
        return ResponseEntity.ok(ApiResponse(true, result))
    }
}
