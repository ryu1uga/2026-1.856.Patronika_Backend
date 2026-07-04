package pe.edu.ulima.patronika.controllers

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.dto.SuspendUserRequest
import pe.edu.ulima.patronika.dto.UserChangePasswordRequest
import pe.edu.ulima.patronika.dto.UserRequest
import pe.edu.ulima.patronika.dto.UserUpdateRequest
import pe.edu.ulima.patronika.dto.VerificationCodeRequest
import pe.edu.ulima.patronika.services.UsersService
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UsersController (
    private val usersService: UsersService,
) {
    @GetMapping
    fun loadAllUsers(): ResponseEntity<ApiResponse<List<User>>> {
        val user = usersService.getAll()
        return ResponseEntity.ok(ApiResponse(true, user))
    }

    @GetMapping("/{id}")
    fun loadUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<User>> {
        val user = usersService.getUser(id)
        return ResponseEntity.ok(ApiResponse(true, user))
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @SwaggerRequestBody(
        content = [Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            encoding = [Encoding(name = "userRequest", contentType = MediaType.APPLICATION_JSON_VALUE)]
        )]
    )
    fun postUser(
        @RequestPart("userRequest") @Valid userRequest: UserRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): ResponseEntity<ApiResponse<User>> {
        val insertedUser = usersService.insertUser(userRequest, file)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(true, insertedUser))
    }

    @PutMapping("/{id}")
    fun putUser(
        @PathVariable id: UUID,
        @Valid @RequestBody userRequest: UserUpdateRequest
    ): ResponseEntity<ApiResponse<String>> {
        usersService.updateUser(id, userRequest)
        return ResponseEntity.ok(ApiResponse(true, "Usuario modificado exitosamente"))
    }

    @PutMapping(
        "/{id}/profile-image",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updateProfileImage(
        @PathVariable id: UUID,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<User>> {
        val updatedUser = usersService.updateProfileImage(id, file)
        return ResponseEntity.ok(ApiResponse(true, updatedUser))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<String>> {
        usersService.deleteUser(id)
        return ResponseEntity.ok(ApiResponse(true, "Usuario eliminado satisfactoriamente"))
    }

    @PostMapping("/change-email/request-code")
    fun requestEmailChangeCode(
        @RequestBody body: VerificationCodeRequest
    ): ResponseEntity<ApiResponse<String>> {
        usersService.requestEmailChangeCode(body.email)
        return ResponseEntity.ok(ApiResponse(true, "Código enviado al nuevo correo"))
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: UserChangePasswordRequest
    ): ResponseEntity<ApiResponse<String>> {
        usersService.changePassword(body)
        return ResponseEntity.ok(ApiResponse(true, "Contraseña actualizada exitosamente"))
    }

    @PostMapping("/{id}/suspend")
    fun suspendUser(
        @PathVariable id: UUID,
        @RequestBody body: SuspendUserRequest
    ): ResponseEntity<ApiResponse<String>> {
        usersService.suspendUser(body.adminId, id, body.days, body.reason)
        return ResponseEntity.ok(ApiResponse(true, "Usuario suspendido exitosamente"))
    }
}