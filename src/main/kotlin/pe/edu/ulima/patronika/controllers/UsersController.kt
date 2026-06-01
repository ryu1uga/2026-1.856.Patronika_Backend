package pe.edu.ulima.patronika.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.ApiResponse
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.dto.UserRequest
import pe.edu.ulima.patronika.services.UsersService
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UsersController (
    private val usersService: UsersService
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
    fun postUser(
        @Valid @RequestPart("userRequest") userRequest: UserRequest,
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
        @Valid @RequestBody userRequest: UserRequest
    ): ResponseEntity<ApiResponse<String>> {
        usersService.updateUser(id, userRequest)
        return ResponseEntity.ok(ApiResponse(true, "Usuario modificado exitosamente"))
    }

    @DeleteMapping("/{id}/{username}")
    fun deleteUser(@PathVariable id: UUID, @PathVariable username: String): ResponseEntity<ApiResponse<String>> {
        usersService.deleteUser(username, id)
        return ResponseEntity.ok(ApiResponse(true, "Usuario eliminado satisfactoriamente"))
    }
}