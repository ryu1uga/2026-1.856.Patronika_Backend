package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class CommentResponse(
    val id: UUID?,
    val userId: UUID?,
    val publicationId: UUID?,
    val content: String,
    val reportCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant?
)
