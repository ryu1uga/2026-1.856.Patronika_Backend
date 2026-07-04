package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank

data class ForgotPasswordRequest(
    @field:NotBlank val email: String,
    @field:NotBlank var password: String,
)

