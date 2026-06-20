package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class PublicationResponseDto(
    val id: UUID?,
    val user: UserSummaryDto,
    val patternId: UUID?,
    val description: String,
    val technique: Int,
    val imageUrl: String?,
    val publishedAt: Instant?
)
