package pe.edu.ulima.patronika.dto

import java.util.UUID

data class PatternLibraryRequest(
    val userId: UUID,
    val patternId: UUID
)
