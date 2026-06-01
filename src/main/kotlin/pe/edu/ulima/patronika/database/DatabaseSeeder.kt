package pe.edu.ulima.patronika.database

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pe.edu.ulima.patronika.database.model.*
import pe.edu.ulima.patronika.database.repository.*
import pe.edu.ulima.patronika.security.HashEncoder
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Component
class DatabaseSeeder(
    private val userRepository: UserRepository,
    private val patternRepository: PatternRepository,
    private val publicationRepository: PublicationRepository,
    private val tutorialRepository: TutorialRepository,
    private val tutorialProgressRepository: TutorialProgressRepository,
    private val commentRepository: CommentRepository,
    private val hashEncoder: HashEncoder
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (userRepository.count() == 0L) {
            println("Iniciando carga de datos de prueba (Patronika Seed)...")

            // 1. Usuarios
            val users = seedUsers()
            
            // 2. Tutoriales
            val tutorials = seedTutorials()
            
            // 3. Patrones
            val patterns = seedPatterns(users)
            
            // 4. Publicaciones
            val publications = seedPublications(users, patterns)
            
            // 5. Comentarios
            seedComments(users, publications)
            
            // 6. Progreso de Tutoriales
            seedTutorialProgress(users, tutorials)

            println("Carga de datos completada exitosamente.")
        } else {
            println("La base de datos ya contiene datos. Se omitió el seed.")
        }
    }

    private fun seedUsers(): List<User> {
        val admin = User(
            username = "admin",
            email = "admin@patronika.com",
            hashedPassword = hashEncoder.encode("admin123"),
            profileImageUrl = "https://example.com/images/admin.jpg",
            isAdmin = true,
            status = 0
        )
        val juan = User(
            username = "juan_perez",
            email = "juan@gmail.com",
            hashedPassword = hashEncoder.encode("juan123"),
            profileImageUrl = "https://example.com/images/admin.jpg",
            isAdmin = false,
            status = 0
        )
        val maria = User(
            username = "maria_tejidos",
            email = "maria@tejidos.com",
            hashedPassword = hashEncoder.encode("maria123"),
            profileImageUrl = "https://example.com/images/admin.jpg",
            isAdmin = false,
            status = 0
        )

        return userRepository.saveAll(listOf(admin, juan, maria))
    }

    private fun seedTutorials(): List<Tutorial> {
        val t1 = Tutorial(
            title = "Introducción al Crochet",
            description = "Aprende los puntos básicos para empezar tus proyectos de crochet.",
            difficulty = 0, // Básico
            url = "https://www.youtube.com/watch?v=crochet-basico"
        )
        val t2 = Tutorial(
            title = "Técnicas de Dos Agujas",
            description = "Domina el arte del tejido a dos agujas desde cero.",
            difficulty = 1, // Intermedio
            url = "https://www.youtube.com/watch?v=dos-agujas"
        )
        val t3 = Tutorial(
            title = "Taller de Amigurumis Avanzado",
            description = "Crea figuras complejas y detalladas con estas técnicas avanzadas.",
            difficulty = 2, // Avanzado
            url = "https://www.youtube.com/watch?v=amigurumi-pro"
        )

        return tutorialRepository.saveAll(listOf(t1, t2, t3))
    }

    private fun seedPatterns(users: List<User>): List<Pattern> {
        val maria = users.first { it.username == "maria_tejidos" }
        
        val p1 = Pattern(
            user = maria,
            name = "Oso de Peluche Amigurumi",
            size = 0, // Small
            isPublic = true,
            publishedAt = Instant.now()
        )
        val p2 = Pattern(
            user = maria,
            name = "Bufanda de Invierno",
            size = 1, // Medium
            isPublic = false
        )

        return patternRepository.saveAll(listOf(p1, p2))
    }

    private fun seedPublications(users: List<User>, patterns: List<Pattern>): List<Publication> {
        val maria = users.first { it.username == "maria_tejidos" }
        val osoPattern = patterns.first { it.name == "Oso de Peluche Amigurumi" }
        
        val pub = Publication(
            user = maria,
            pattern = osoPattern,
            description = "Acabo de terminar este osito, ¡me encantó el resultado!",
            technique = 0, // Crochet
            imageUrl = "https://example.com/images/oso.jpg",
            publishedAt = Instant.now()
        )

        return listOf(publicationRepository.save(pub))
    }

    private fun seedComments(users: List<User>, publications: List<Publication>) {
        val juan = users.first { it.username == "juan_perez" }
        val pub = publications.first()

        val comment = Comment(
            user = juan,
            publication = pub,
            content = "¡Qué lindo patrón! Gracias por compartir."
        )

        commentRepository.save(comment)
    }

    private fun seedTutorialProgress(users: List<User>, tutorials: List<Tutorial>) {
        val juan = users.first { it.username == "juan_perez" }
        val t1 = tutorials.first()

        val progress = TutorialProgress(
            user = juan,
            tutorial = t1,
            status = 1, // Complete
            registeredDate = LocalDate.now()
        )

        tutorialProgressRepository.save(progress)
    }
}
