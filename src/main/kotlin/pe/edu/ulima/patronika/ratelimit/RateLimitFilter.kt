package pe.edu.ulima.patronika.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pe.edu.ulima.patronika.ApiResponse
import java.util.concurrent.TimeUnit
import io.github.bucket4j.Bandwidth as B4jBandwidth

@Component
class RateLimitFilter(
    private val props: RateLimitProperties,
    private val objectMapper: ObjectMapper
): OncePerRequestFilter() {

    private val bucketCache: Cache<String, Bucket> = Caffeine
        .newBuilder()
        .maximumSize(props.cache.maximumSize)
        .expireAfterAccess(props.cache.expireAfterAccess)
        .build()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (!props.enabled) return true

        if (request.method.equals("OPTIONS", ignoreCase = true)) return true

        val path = request.requestURI ?: ""

        return path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs/") ||
            path == "/actuator/health" ||
            path == "/api/healthcheck"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val key = buildKey(request)
        val bucket = bucketCache.get(key) { newBucketFor(request) }

        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            response.setHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            filterChain.doFilter(request, response)
            return
        }

        val nanosToWait = probe.nanosToWaitForRefill
        val secondsToWait = TimeUnit.NANOSECONDS.toSeconds(nanosToWait).coerceAtLeast(1)

        response.status = 429
        response.setHeader(HttpHeaders.RETRY_AFTER, secondsToWait.toString())
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val body = ApiResponse(false, "Demasiadas solicitudes")
        response.writer.use { writer ->
            writer.write(objectMapper.writeValueAsString(body))
        }
    }

    private fun newBucketFor(request: HttpServletRequest): Bucket {
        val bandwidthProps = selectBandwidth(request)
        val limit = B4jBandwidth.classic(
            bandwidthProps.capacity,
            Refill.intervally(bandwidthProps.refillTokens, bandwidthProps.refillPeriod)
        )

        return Bucket.builder()
            .addLimit(limit)
            .build()
    }

    private fun selectBandwidth(request: HttpServletRequest): RateLimitProperties.Bandwidth {
        val path = request.requestURI ?: ""
        return if (path == "/api/auth/login" || path == "/api/auth/loginWS") {
            props.login
        } else {
            props.default
        }
    }

    private fun buildKey(request: HttpServletRequest): String {
        val authentication = SecurityContextHolder.getContext()?.authentication
        val principal = authentication?.principal

        if (authentication != null && authentication.isAuthenticated && principal != null && principal != "anonymousUser") {
            return "user:$principal"
        }

        val ip = extractClientIp(request)
        return "ip:$ip"
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val xff = request.getHeader("X-Forwarded-For")
        if (!xff.isNullOrBlank()) {
            return xff.split(',')
                .firstOrNull()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: request.remoteAddr
        }

        val realIp = request.getHeader("X-Real-IP")
        if (!realIp.isNullOrBlank()) return realIp.trim()

        return request.remoteAddr
    }
}
