package pe.edu.ulima.patronika.database.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "published_patterns",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "pattern_id"])]
)
class PublishedPattern(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pattern_id", nullable = false)
    var pattern: Pattern = Pattern(),

    @Column(nullable = false)
    var publishedAt: Instant = Instant.now()
)
