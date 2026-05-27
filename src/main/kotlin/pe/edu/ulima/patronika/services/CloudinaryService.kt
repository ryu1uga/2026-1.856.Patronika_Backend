package pe.edu.ulima.patronika.services

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryService(
    @Value("\${cloudinary.cloud-name}") private val cloudName: String,
    @Value("\${cloudinary.api-key}") private val apiKey: String,
    @Value("\${cloudinary.api-secret}") private val apiSecret: String
) {
    private val cloudinary: Cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        )
    )

    fun uploadImage(file: MultipartFile, folder: String = "users"): String {
        if (file.isEmpty) throw IllegalArgumentException("El archivo no puede estar vacío")

        val options = ObjectUtils.asMap(
            "folder", folder,
            "resource_type", "image"
        )

        val uploadResult = cloudinary.uploader().upload(file.bytes, options)
        return uploadResult["secure_url"] as String
    }

    fun deleteImage(imageUrl: String) {
        try {
            // Extraer el public_id de la URL de Cloudinary
            // Ejemplo URL: https://res.cloudinary.com/demo/image/upload/v123456/patterns/mi_foto.jpg
            // El public_id que necesitamos es: "patterns/mi_foto"
            val publicId = imageUrl
                .substringAfter("/upload/")       // Nos quedamos con "v123456/patterns/mi_foto.jpg"
                .substringAfter("/")              // Quitamos la versión ("v123456/"), queda "patterns/mi_foto.jpg"
                .substringBeforeLast(".")         // Quitamos la extensión (".jpg"), queda "patterns/mi_foto"

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
        } catch (e: Exception) {
            // Puedes usar un logger aquí. No bloqueamos el flujo si el borrado en Cloudinary falla.
            println("Error al eliminar la imagen de Cloudinary: ${e.message}")
        }
    }
}