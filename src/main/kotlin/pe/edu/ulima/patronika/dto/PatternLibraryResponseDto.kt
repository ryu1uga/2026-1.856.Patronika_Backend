package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class PatternLibraryResponseDto(
    val id: UUID?,
    val userId: UUID?,
    val pattern: PatternResponseDto,
    val savedAt: Instant
)
