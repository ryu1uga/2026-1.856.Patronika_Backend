package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.dto.*
import pe.edu.ulima.patronika.security.AuthService
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
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

    @PostMapping("/register/request-code")
    fun requestCode(@Valid @RequestBody body: VerificationCodeRequest): ResponseEntity<ApiResponse<String>> {
        authService.requestVerificationCode(body.email)
        return ResponseEntity.ok(ApiResponse(true, "Código enviado al correo"))
    }

    @PostMapping("/register/verify-code")
    fun verifyCode(@Valid @RequestBody body: VerifyCodeRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = authService.verifyCode(body.email, body.code)
        return ResponseEntity.ok(ApiResponse(true, result))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody body: RegisterRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        val result = authService.register(body.verificationToken, body.user)
        return ResponseEntity.ok(ApiResponse(true, result))
    }
}