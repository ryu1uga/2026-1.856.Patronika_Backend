package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class TutorialRequest (
    val title: String = "",
    val description: String = "",
    val difficulty: Int = 0,
    val url: String = "",
)