package pe.edu.ulima.patronika.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var username: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var hashedPassword: String = "",

    @Column(nullable = false)
    var isAdmin: Boolean = false,

    @Column(nullable = false)
    var status: Int = 0, //0 for active, 1 for suspended, 2 for blocked

    @Column(nullable = false)
    var registeredDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var activateNotification: Boolean = true,

    @Column(nullable = false)
    var suspensionEndDate: LocalDateTime? = null
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val patterns: MutableList<Pattern> = mutableListOf()
}