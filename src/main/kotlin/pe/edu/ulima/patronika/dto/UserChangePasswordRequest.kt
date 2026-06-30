package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UserChangePasswordRequest(
    val userId: UUID,
    @field:NotBlank val currentPassword: String,
    @field:NotBlank val newPassword: String
)
