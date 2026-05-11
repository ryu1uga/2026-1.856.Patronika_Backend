package pe.edu.ulima.patronika.database.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "tutorials")
class Tutorial (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(nullable = false)
    var description: String = "",

    @Column(nullable = false)
    var difficulty: Int = 0, //0 for basic, 1 for intermediate, 2 for advanced

    @Column(nullable = false)
    var url: String = "",
) {
    @OneToMany(mappedBy = "tutorial", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val tutorialProgresses: MutableList<TutorialProgress> = mutableListOf()
}