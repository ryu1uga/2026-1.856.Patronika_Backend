package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class UserRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val email: String,
    var password: String,
    val isAdmin: Boolean = false,
)
