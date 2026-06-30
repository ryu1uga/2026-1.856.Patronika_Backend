package pe.edu.ulima.patronika.dto

import java.time.Instant
import java.util.UUID

data class PatternLibraryResponse(
    val id: UUID?,
    val userId: UUID?,
    val pattern: PatternResponse,
    val savedAt: Instant
)
