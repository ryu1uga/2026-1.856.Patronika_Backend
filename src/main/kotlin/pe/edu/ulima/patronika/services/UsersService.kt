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
import pe.edu.ulima.patronika.services.EmailService
import java.time.LocalDate
import java.util.UUID

@Service
class UsersService (
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val cloudinaryService: CloudinaryService,
    private val emailService: EmailService
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

        if(userRepository.findByEmail(userRequest.email) != null) {
            throw ConflictException("El correo ya está registrado")
        }

        val uploadedUrl = if (file != null && !file.isEmpty) {
            cloudinaryService.uploadImage(file, folder = "users")
        } else null

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
        req: UserUpdateRequest
    ) {
        val user = getUser(id)

        val userWithSameEmail = userRepository.findByEmail(req.email)
        if (userWithSameEmail != null && userWithSameEmail.id != id) {
            throw ConflictException("El correo ya está registrado")
        }

        user.username = req.username
        user.email = req.email
        req.isAdmin?.let { user.isAdmin = it }
        req.status?.let { user.status = it }
        req.suspensionEndDate.let { user.suspensionEndDate = it }

        userRepository.save(user)
    }

    fun updateProfileImage(
        id: UUID,
        file: MultipartFile
    ): User {
        val user = getUser(id)

        user.profileImageUrl?.let {
            cloudinaryService.deleteImage(it)
        }

        val uploadedUrl = cloudinaryService.uploadImage(file, folder = "users")
        user.profileImageUrl = uploadedUrl

        return userRepository.save(user)
    }

    fun deleteUser(targetId: UUID) {
        val user = userRepository.findById(targetId).orElseThrow { NotFoundException() }
        userRepository.delete(user)
    }

    fun suspendUser(adminId: UUID, targetId: UUID, days: Int, reason: String) {
        val admin = getUser(adminId)
        if (admin.isAdmin != true) throw UnauthorizedException()

        val target = getUser(targetId)
        target.status = 1
        target.suspensionEndDate = LocalDate.now().plusDays(days.toLong())
        userRepository.save(target)

        emailService.sendSuspensionEmail(
            toEmail = target.email,
            username = target.username,
            reason = reason,
            days = days,
            endDate = target.suspensionEndDate!!
        )
    }
}