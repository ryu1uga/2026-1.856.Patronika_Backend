package pe.edu.ulima.patronika.services

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pe.edu.ulima.patronika.database.model.Pattern
import pe.edu.ulima.patronika.database.model.User
import pe.edu.ulima.patronika.database.repository.PatternRepository
import pe.edu.ulima.patronika.database.repository.UserRepository
import pe.edu.ulima.patronika.dto.PatternRequest
import pe.edu.ulima.patronika.exception.BadRequestException
import pe.edu.ulima.patronika.exception.NotFoundException
import java.util.UUID

@Service
class PatternsService (
    private val patternRepository: PatternRepository,
    private val userRepository: UserRepository
) {
    fun getAll(): List<Pattern> = patternRepository.findAll()

    fun getPattern(id: UUID): Pattern {
        return patternRepository.findById(id).orElseThrow { NotFoundException() }
    }

    private fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow { BadRequestException("Usuario no registrado") }
    }

    fun insertPattern(
        userId: UUID,
        patternRequest: PatternRequest
    ): Pattern {
        val user = getUser(userId)

        val pattern = Pattern(
            name = patternRequest.name,
            size = patternRequest.size,
            user = user
        )
        return patternRepository.save(pattern)
    }

    fun updatePattern(
        id: UUID,
        req: PatternRequest
    ) {
        val pattern = getPattern(id)

        pattern.name = req.name
        pattern.size = req.size

        patternRepository.save(pattern)
    }

    fun deletePattern(id: UUID) {
        if (!patternRepository.existsById(id)) throw NotFoundException()
        patternRepository.deleteById(id)
    }
}