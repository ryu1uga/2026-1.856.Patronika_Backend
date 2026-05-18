package pe.edu.ulima.patronika.security

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import pe.edu.ulima.patronika.ratelimit.RateLimitFilter

@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val rateLimitFilter: RateLimitFilter
) {
    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/healthcheck",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/auth/login",
                        "/api/auth/refresh"
                    )
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .dispatcherTypeMatchers(
                        DispatcherType.ERROR,
                        DispatcherType.FORWARD
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .exceptionHandling { configurer ->
                configurer
                    .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(rateLimitFilter, JwtAuthFilter::class.java)
            .build()
    }
}