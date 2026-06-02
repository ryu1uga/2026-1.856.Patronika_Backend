package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank val email: String,
    var password: String,
)

