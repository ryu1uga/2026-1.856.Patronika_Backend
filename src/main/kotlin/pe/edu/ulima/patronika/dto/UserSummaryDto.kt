package pe.edu.ulima.patronika.dto

import java.util.UUID

data class UserSummaryDto(
    val id: UUID?,
    val username: String,
    val profileImageUrl: String?
)
