package pe.edu.ulima.patronika.exception

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pe.edu.ulima.patronika.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(false, e.message))

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(false, e.message))

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(false, e.message))

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(e: BadCredentialsException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse(false, e.message ?: "Credenciales inválidas"))

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse(false, e.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<List<String>>> {
        val errors = e.bindingResult
            .fieldErrors
            .map { "${it.field}: ${it.defaultMessage ?: "Valor inválido"}" }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(false, errors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(false, "Formato de solicitud inválido. Asegúrate de enviar JSON válido con Content-Type: application/json"))

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(e: DataIntegrityViolationException): ResponseEntity<ApiResponse<String>> {
        val rootCause = e.rootCause?.message ?: ""
        return if (rootCause.contains("unique constraint") || rootCause.contains("duplicate key")) {
            ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse(false, "Ya existe un registro con esos datos"))
        } else {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(false, "Error de integridad de datos"))
        }
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ApiResponse(false, "Tipo de contenido no soportado: ${e.contentType}. Asegúrate de enviar la parte JSON (ej. 'userRequest' o 'publication') con Content-Type: application/json"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ApiResponse<String>> {
        logger.warn("Solicitud inválida: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse(false, e.message ?: "Solicitud inválida"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<ApiResponse<String>> {
        logger.error("Error interno no controlado", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(false, "Error interno"))
    }
}
