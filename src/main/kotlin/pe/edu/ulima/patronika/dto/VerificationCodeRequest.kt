package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class VerificationCodeRequest(
    @field:NotBlank @field:Email val email: String
)