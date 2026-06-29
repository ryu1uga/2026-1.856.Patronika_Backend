package pe.edu.ulima.patronika.dto

import java.util.UUID

data class DeletePublicationRequest(
    val adminId: UUID,
    val reason: String
)
