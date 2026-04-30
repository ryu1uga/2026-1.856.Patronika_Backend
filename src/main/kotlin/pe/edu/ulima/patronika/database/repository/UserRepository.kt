package pe.edu.ulima.patronika.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import pe.edu.ulima.patronika.database.model.User
import java.util.*


interface UserRepository: JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
}