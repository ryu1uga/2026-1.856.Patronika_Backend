package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class VerifyCodeRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 4, max = 4) val code: String
)