package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class PublishedPatternResponse(
    val id: UUID?,
    val userId: UUID?,
    val patternId: UUID?,
    val publishedAt: Instant
)
