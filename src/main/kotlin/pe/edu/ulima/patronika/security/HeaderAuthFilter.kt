package pe.edu.ulima.patronika.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HeaderAuthFilter(
    private val headerService: HeaderService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userIdHeader = request.getHeader("UserId")
        val rptaHeader = request.getHeader("Rpta")
        val tokenHeader = request.getHeader("Token")

        if (!userIdHeader.isNullOrBlank()
            && !rptaHeader.isNullOrBlank()
            && !tokenHeader.isNullOrBlank()) {
            val authenticatedUser = headerService.authenticate(
                userIdHeader,
                rptaHeader,
                tokenHeader
            )

            if (authenticatedUser != null) {
                val auth = UsernamePasswordAuthenticationToken(
                    authenticatedUser.userId,
                    null,
                    emptyList()
                ).apply {
                    details = mapOf(
                        "rpta" to authenticatedUser.rpta,
                        "token" to authenticatedUser.token
                    )
                }

                SecurityContextHolder.getContext().authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }
}