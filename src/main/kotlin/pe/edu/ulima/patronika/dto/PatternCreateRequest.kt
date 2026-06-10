package pe.edu.ulima.patronika.dto

import org.springframework.web.multipart.MultipartFile

data class PatternCreateRequest (
    val name: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val image: MultipartFile? = null
)