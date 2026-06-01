package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.dto.*
import pe.edu.ulima.patronika.security.AuthService
import pe.edu.ulima.patronika.services.UsersService
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UsersService
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody body: AuthRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        val loginResponse = authService.login(body.username, body.password)
        return ResponseEntity.ok(ApiResponse(true, loginResponse))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody body: RefreshTokenRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        val tokenPair = authService.refresh(body.refreshToken)
        return ResponseEntity.ok(ApiResponse(true, tokenPair))
    }

    @PostMapping("/logout/{id}")
    fun logout(
        @PathVariable id: UUID,
        @Valid @RequestBody body: RefreshTokenRequest
    ): ResponseEntity<ApiResponse<String>> {
        authService.logout(id, body.refreshToken)
        return ResponseEntity.ok(ApiResponse(true, "Cerró sesión exitosamente"))
    }

    @PostMapping("/verify-code")
    fun verifyCode(@Valid @RequestBody body: VerifyCodeRequest): ResponseEntity<ApiResponse<String>> {
        authService.verifyCode(body.email, body.code)
        return ResponseEntity.ok(ApiResponse(true, "Código verificado exitosamente"))
    }

    @PostMapping("/register/request-code")
    fun requestCode(@Valid @RequestBody body: VerificationCodeRequest): ResponseEntity<ApiResponse<String>> {
        authService.requestVerificationCode(body.email)
        return ResponseEntity.ok(ApiResponse(true, "Código enviado al correo"))
    }

    @PostMapping(
        "/register",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun register(
        @Valid @RequestPart("userRequest") body: UserRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<ApiResponse<User>> {
        val result = userService.insertUser(body, file)
        return ResponseEntity.ok(ApiResponse(true, result))
    }

    @PostMapping("/change-password/request-code")
    fun requestCodeForExistingEmail(@Valid @RequestBody body: VerificationCodeRequest): ResponseEntity<ApiResponse<String>> {
        authService.requestVerificationCodeOnExistingEmail(body.email)
        return ResponseEntity.ok(ApiResponse(true, "Código enviado al correo"))
    }

    @PostMapping("/change-password")
    fun changePassword(@Valid @RequestBody body: UserRequest): ResponseEntity<ApiResponse<String>> {
        authService.changePassword(body)
        return ResponseEntity.ok(ApiResponse(true, "Contraseña cambiada exitosamente"))
    }
}