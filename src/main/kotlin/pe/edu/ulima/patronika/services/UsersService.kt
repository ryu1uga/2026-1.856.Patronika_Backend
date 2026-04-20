package pe.edu.ulima.patronika.services

import pe.edu.ulima.patronika.model.User
import pe.edu.ulima.patronika.repository.UserRepository

class UsersService (
    private val userRepository: UserRepository
) {
    fun getAll(): List<User> = userRepository.findAll()
}