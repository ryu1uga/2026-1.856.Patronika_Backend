package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class PatternResponseDto(
    val id: UUID?,
    val userId: UUID?,
    val name: String,
    val gridData: String?,
    val width: Int,
    val height: Int,
    val isPublic: Boolean,
    val publishedAt: Instant?,
    val createdAt: Instant
)
