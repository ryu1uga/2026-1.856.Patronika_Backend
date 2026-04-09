package pe.edu.ulima.patronika

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null
)