package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:NotBlank val verificationToken: String,
    val user: UserRequest
)