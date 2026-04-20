package pe.edu.ulima.patronika.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tutorialProgresses")
    class TutorialProgress (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @Column(nullable = false)
    var status: Int = 0, //0 for in-progress, 1 for complete

    var registeredDate: LocalDateTime? = null,
)