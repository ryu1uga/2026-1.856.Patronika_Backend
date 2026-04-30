package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class UserRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val email: String,
    val isAdmin: Boolean = false,
    val status: Int = 0,
    val activateNotification: Boolean = true,
    val suspensionEndDate: LocalDateTime? = null,
)
