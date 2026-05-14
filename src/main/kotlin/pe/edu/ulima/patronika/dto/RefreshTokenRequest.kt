package pe.edu.ulima.patronika.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank val refreshToken: String
)
