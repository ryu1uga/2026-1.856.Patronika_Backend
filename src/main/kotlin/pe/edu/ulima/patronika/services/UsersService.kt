package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.*
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.security.HashEncoder
import java.util.UUID

@Service
class UsersService (
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val cloudinaryService: CloudinaryService
) {
    fun getAll(): List<User> = userRepository.findAll()

    fun getUser(id: UUID): User {
        return userRepository.findById(id).orElseThrow { NotFoundException() }
    }

    fun insertUser(
        userRequest: UserRequest,
        file: MultipartFile?
    ): User {
        if(userRepository.findByUsername(userRequest.username) != null) {
            throw ConflictException("Usuario ya existe")
        }

        val uploadedUrl = file?.let {
            cloudinaryService.uploadImage(it, folder = "users")
        }

        val userEntity = User(
            username = userRequest.username,
            email = userRequest.email,
            hashedPassword = hashEncoder.encode(userRequest.password),
            profileImageUrl = uploadedUrl,
            isAdmin = userRequest.isAdmin,
            status = userRequest.status,
            activateNotification = userRequest.activateNotification,
            suspensionEndDate = userRequest.suspensionEndDate
        )

        return userRepository.save(userEntity)
    }

    fun updateUser(
        id: UUID,
        req: UserRequest
    ) {
        val user = getUser(id)

        user.username = req.username
        user.email = req.email
        user.isAdmin = req.isAdmin
        user.status = req.status
        user.activateNotification = req.activateNotification
        user.suspensionEndDate = req.suspensionEndDate

        userRepository.save(user)
    }

    fun deleteUser(
        username: String,
        userId: UUID
    ) {
        val userAdmin = getUser(userId)

        if(userAdmin.isAdmin != true) throw UnauthorizedException()

        val user = userRepository.findByUsername(username)
            ?: throw NotFoundException()

        userRepository.delete(user)
    }
}