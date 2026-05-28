package pe.edu.ulima.patronika.database.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patterns")
class Pattern (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON) //
    var gridData: String? = null,

    @Column(nullable = false)
    var size: Int = 0,

    @Column(nullable = false)
    var isPublic: Boolean = false,

    var publishedAt: Instant? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
) {
    @OneToMany(mappedBy = "pattern", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    val publications: MutableList<Publication> = mutableListOf()
}