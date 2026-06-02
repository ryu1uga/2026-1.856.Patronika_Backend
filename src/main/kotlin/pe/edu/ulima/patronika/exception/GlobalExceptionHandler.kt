package pe.edu.ulima.patronika.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pe.edu.ulima.patronika.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<ApiResponse<String>> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse(false, "Error interno"))
}