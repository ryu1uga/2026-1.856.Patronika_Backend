package pe.edu.ulima.patronika.database.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "publications")
    class Publication (
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
    var description: String = "",

    var publishedAt: Instant? = null,
) {
    @OneToMany(mappedBy = "publication", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val comments: MutableList<Comment> = mutableListOf()
}