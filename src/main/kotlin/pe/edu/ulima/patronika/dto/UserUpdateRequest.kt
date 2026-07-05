package pe.edu.ulima.patronika.dto

import java.time.LocalDate

data class UserUpdateRequest(
    val username: String? = null,
    val email: String? = null,
    val isAdmin: Boolean? = null,
    val status: Int? = null,
    val suspensionEndDate: LocalDate? = null
)
