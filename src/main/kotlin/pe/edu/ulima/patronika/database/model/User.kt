package pe.edu.ulima.patronika.database.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
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

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    @Column(nullable = false)
    var isAdmin: Boolean = false,

    @Column(name = "logged_in", nullable = false)
    var loggedIn: Boolean = false,

    @Column(nullable = false)
    var status: Int = 0, //0 for active, 1 for suspended, 2 for blocked

    @Column(nullable = false)
    var registeredDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var activateNotification: Boolean = true,

    var suspensionEndDate: LocalDate? = null,

    var token: String = ""
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val patterns: MutableList<Pattern> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val publications: MutableList<Publication> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val comments: MutableList<Comment> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val tutorialProgresses: MutableList<TutorialProgress> = mutableListOf()
}