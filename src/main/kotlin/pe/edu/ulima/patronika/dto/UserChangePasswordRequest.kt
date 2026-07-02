package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UserChangePasswordRequest(
    val email: String,
    @field:NotBlank val currentPassword: String,
    @field:NotBlank val newPassword: String
)
