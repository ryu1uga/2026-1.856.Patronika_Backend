package pe.edu.ulima.patronika.dto

import java.time.LocalDate
import java.util.UUID

data class TutorialProgressResponse(
    val id: UUID?,
    val userId: UUID?,
    val tutorialId: UUID?,
    val status: Int,
    val registeredDate: LocalDate?
)
