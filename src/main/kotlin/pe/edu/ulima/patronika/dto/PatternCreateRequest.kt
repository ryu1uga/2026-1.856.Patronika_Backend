package pe.edu.ulima.patronika.dto

import org.springframework.web.multipart.MultipartFile

data class PatternCreateRequest (
    val name: String = "",
    val size: Int = 0,  // 0=small(16x16), 1=medium(32x32), 2=large(64x64)
    val image: MultipartFile? = null
)