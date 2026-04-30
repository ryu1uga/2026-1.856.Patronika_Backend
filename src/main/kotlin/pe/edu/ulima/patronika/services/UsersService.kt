package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.*
import pe.edu.ulima.patronika.exception.NotFoundException
import pe.edu.ulima.patronika.exception.UnauthorizedException
import java.util.UUID

@Service
class UsersService (
    private val userRepository: UserRepository
) {
    fun getAll(): List<User> = userRepository.findAll()

    fun getUser(id: UUID): User {
        return userRepository.findById(id).orElseThrow { NotFoundException() }
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