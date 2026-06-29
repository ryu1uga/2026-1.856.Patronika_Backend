package pe.edu.ulima.patronika.dto

import java.util.UUID

data class SuspendUserRequest(
    val adminId: UUID,
    val days: Int,
    val reason: String
)
