package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class CommentResponseDto(
    val id: UUID?,
    val userId: UUID?,
    val publicationId: UUID?,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
