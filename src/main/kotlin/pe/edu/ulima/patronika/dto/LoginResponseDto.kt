package pe.edu.ulima.patronika.dto

import java.time.LocalDate

data class LoginResponseDto(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val status: Int,
    val suspensionEndDate: LocalDate? = null,
    val suspensionDaysRemaining: Long? = null
)
