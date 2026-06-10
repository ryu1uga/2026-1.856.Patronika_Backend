package pe.edu.ulima.patronika.dto

import java.util.UUID

data class CommentRequest (
    val content: String = "",
    val publicationId: UUID? = null,
)
