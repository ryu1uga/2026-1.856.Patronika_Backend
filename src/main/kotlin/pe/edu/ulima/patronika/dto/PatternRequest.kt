package pe.edu.ulima.patronika.dto

import java.time.Instant

data class PatternRequest (
    val name: String = "",
    val imageUrl: String = "",
    val gridData: String? = null,
    val size: Int = 0,
    val difficulty: Int = 0,
    val technique: Int = 0,
    val isPublic: Boolean = false,
    val publishedAt: Instant? = null,
)