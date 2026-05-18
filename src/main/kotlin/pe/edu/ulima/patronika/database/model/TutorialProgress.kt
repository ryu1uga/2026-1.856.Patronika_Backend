package pe.edu.ulima.patronika.database.model

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "tutorialProgresses")
    class TutorialProgress (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tutorial_id", nullable = false)
    var tutorial: Tutorial = Tutorial(),

    @Column(nullable = false)
    var status: Int = 0, //0 for in-progress, 1 for complete

    var registeredDate: LocalDate? = LocalDate.now(),
)