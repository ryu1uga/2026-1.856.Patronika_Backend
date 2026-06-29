package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class UserUpdateRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val email: String,
    val isAdmin: Boolean? = null,
    val status: Int? = null,
    val suspensionEndDate: LocalDate? = null
)
