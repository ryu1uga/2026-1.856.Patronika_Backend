package pe.edu.ulima.patronika.scheduled

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pe.edu.ulima.patronika.database.repository.EmailVerificationCodeRepository
import pe.edu.ulima.patronika.database.repository.RefreshTokenRepository
import java.time.Instant

/**
 * Borra periódicamente de la base de datos los códigos de verificación de correo
 * y los refresh tokens ya expirados, para que no queden pegados.
 *
 * Un solo método programado ejecuta dos DELETE masivos (una sentencia SQL por tabla,
 * sin cargar entidades) => menor costo computacional.
 *
 * Intervalo configurable vía properties:
 *   app.cleanup.expired.fixed-delay-ms   (por defecto 7 días)
 *   app.cleanup.expired.initial-delay-ms (por defecto 1 min tras el arranque)
 */
@Component
class ExpiredTokenCleanupScheduler(
    private val emailVerificationCodeRepository: EmailVerificationCodeRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(
        fixedDelayString = "\${app.cleanup.expired.fixed-delay-ms}",
        initialDelayString = "\${app.cleanup.expired.initial-delay-ms:60000}"
    )
    @Transactional
    fun cleanupExpired() {
        val now = Instant.now()
        val deletedCodes = emailVerificationCodeRepository.deleteAllExpired(now)
        val deletedTokens = refreshTokenRepository.deleteAllExpired(now)
        log.info(
            "Limpieza de expirados: {} códigos de verificación y {} refresh tokens eliminados",
            deletedCodes, deletedTokens
        )
    }
}
