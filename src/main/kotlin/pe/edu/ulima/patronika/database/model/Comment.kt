package pe.edu.ulima.patronika.database.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "comments")
class Comment (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publication_id", nullable = false)
    var publication: Publication = Publication(),

    @Column(nullable = false)
    var content: String = "",

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    var updatedAt: Instant? = null,
)