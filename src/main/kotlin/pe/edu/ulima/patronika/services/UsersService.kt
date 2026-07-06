package pe.edu.ulima.patronika.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.database.model.EmailVerificationCodeEntity
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.EmailVerificationCodeRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.*
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.ConflictException
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import pe.edu.ulima.patronika.security.HashEncoder
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

@Service
class UsersService (
    private val userRepository: UserRepository,
    private val hashEncoder: HashEncoder,
    private val cloudinaryService: CloudinaryService,
    private val emailService: EmailService,
    private val emailVerificationCodeRepository: EmailVerificationCodeRepository,
    @Value("\${app.verification.code.expiry-ms}") private val codeExpiryMs: Long
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
        )

        return userRepository.save(userEntity)
    }

    fun updateUser(
        id: UUID,
        req: UserUpdateRequest
    ) {
        val user = getUser(id)

        if (req.username != null) {
            val userWithSameUsername = userRepository.findByUsername(req.username)
            if (userWithSameUsername != null && userWithSameUsername.id != id) {
                throw ConflictException("Ese nombre de usuario ya existe, por favor elige otro")
            }
            user.username = req.username
        }

        if (req.email != null) {
            val userWithSameEmail = userRepository.findByEmail(req.email)
            if (userWithSameEmail != null && userWithSameEmail.id != id) {
                throw ConflictException("Ese correo ya existe, por favor elige otro")
            }
            user.email = req.email
        }

        req.isAdmin?.let { user.isAdmin = it }

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

    @Transactional
    fun requestEmailChangeCode(currentEmail: String) {
        // El código se envía al correo ACTUAL registrado en la base de datos,
        // por lo que ese correo debe existir.
        if (userRepository.findByEmail(currentEmail) == null) {
            throw NotFoundException("No existe un usuario con ese correo")
        }

        emailVerificationCodeRepository.deleteByEmail(currentEmail)

        val code = (100000..999999).random().toString()
        val hashed = hashToken(code)
        val expiresAt = Instant.now().plusMillis(codeExpiryMs)

        emailVerificationCodeRepository.save(
            EmailVerificationCodeEntity(
                email = currentEmail,
                hashedCode = hashed,
                expiresAt = expiresAt
            )
        )

        emailService.sendEmailChangeCode(currentEmail, code)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun changePassword(req: UserChangePasswordRequest) {
        val user = userRepository.findByEmail(req.email)
            ?: throw NotFoundException("No existe un usuario con ese correo")

        if (!hashEncoder.matches(req.currentPassword, user.hashedPassword)) {
            throw BadRequestException("La contraseña actual es incorrecta")
        }

        user.hashedPassword = hashEncoder.encode(req.newPassword)
        userRepository.save(user)
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
        target.suspensionStartDate = LocalDate.now()
        target.suspensionEndDate = LocalDate.now().plusDays(days.toLong())
        target.suspensionReason = reason
        userRepository.save(target)

        emailService.sendSuspensionEmail(
            toEmail = target.email,
            username = target.username,
            reason = reason,
            days = days,
            endDate = target.suspensionEndDate!!
        )
    }

    fun reactivateUser(adminId: UUID, targetId: UUID) {
        val admin = getUser(adminId)
        if (admin.isAdmin != true) throw UnauthorizedException()

        val target = getUser(targetId)
        if (target.status != 1) {
            throw BadRequestException("El usuario no está suspendido")
        }

        target.status = 0
        target.suspensionStartDate = null
        target.suspensionEndDate = null
        target.suspensionReason = null
        userRepository.save(target)

        emailService.sendReactivationEmail(
            toEmail = target.email,
            username = target.username
        )
    }

    /**
     * Reactiva automáticamente a los usuarios cuya suspensión ya venció
     * (status = 1 y suspensionEndDate <= hoy) y les envía el correo de reactivación.
     * Devuelve la cantidad de usuarios reactivados.
     */
    @Transactional
    fun reactivateExpiredSuspensions(): Int {
        val today = LocalDate.now()
        val expired = userRepository.findByStatusAndSuspensionEndDateLessThanEqual(1, today)

        expired.forEach { user ->
            user.status = 0
            user.suspensionStartDate = null
            user.suspensionEndDate = null
            user.suspensionReason = null
            userRepository.save(user)
            emailService.sendReactivationEmail(
                toEmail = user.email,
                username = user.username
            )
        }

        return expired.size
    }
}