package pe.edu.ulima.patronika.scheduled

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pe.edu.ulima.patronika.services.UsersService

/**
 * Reactiva periódicamente a los usuarios cuya suspensión ya venció y les
 * envía el correo de reactivación, sin necesidad de que un admin intervenga.
 *
 * Intervalo configurable vía properties:
 *   app.suspension.reactivate.fixed-delay-ms   (por defecto 1 día)
 *   app.suspension.reactivate.initial-delay-ms (por defecto 1 min tras el arranque)
 */
@Component
class SuspensionReactivationScheduler(
    private val usersService: UsersService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(
        fixedDelayString = "\${app.suspension.reactivate.fixed-delay-ms}",
        initialDelayString = "\${app.suspension.reactivate.initial-delay-ms:60000}"
    )
    fun reactivateExpiredSuspensions() {
        val reactivated = usersService.reactivateExpiredSuspensions()
        if (reactivated > 0) {
            log.info("Reactivación automática: {} usuario(s) reactivado(s) por fin de suspensión", reactivated)
        }
    }
}
