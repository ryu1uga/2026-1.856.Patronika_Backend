package pe.edu.ulima.patronika.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "rate-limit")
data class RateLimitProperties(
    val enabled: Boolean = true,
    val cache: Cache = Cache(),
    val default: Bandwidth = Bandwidth(),
    val login: Bandwidth = Bandwidth(
        capacity = 10,
        refillTokens = 10,
        refillPeriod = Duration.ofMinutes(1)
    )
) {
    data class Cache(
        val maximumSize: Long = 50_000,
        val expireAfterAccess: Duration = Duration.ofMinutes(30)
    )

    data class Bandwidth(
        val capacity: Long = 60,
        val refillTokens: Long = 60,
        val refillPeriod: Duration = Duration.ofMinutes(1)
    )
}
