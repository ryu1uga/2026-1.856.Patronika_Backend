package pe.edu.ulima.patronika.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    @Value("\${app.email.from}") private val fromAddress: String
) {
    fun sendPublicationDeletedEmail(toEmail: String, username: String, reason: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromAddress)
        helper.setTo(toEmail)
        helper.setSubject("Tu publicación ha sido eliminada - Patrónika")
        helper.setText(
            """
            <h2>Hola, $username</h2>
            <p>Tu publicación ha sido eliminada por un administrador de Patrónika.</p>
            <p><strong>Motivo:</strong> $reason</p>
            <p>Si tienes alguna consulta, contacta al soporte.</p>
            """.trimIndent(),
            true
        )

        mailSender.send(message)
    }

    fun sendSuspensionEmail(toEmail: String, username: String, reason: String, days: Int, endDate: java.time.LocalDate) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromAddress)
        helper.setTo(toEmail)
        helper.setSubject("Tu cuenta ha sido suspendida - Patrónika")
        helper.setText(
            """
            <h2>Hola, $username</h2>
            <p>Tu cuenta en Patrónika ha sido suspendida por <strong>$days día(s)</strong>.</p>
            <p><strong>Motivo:</strong> $reason</p>
            <p><strong>Fecha de fin de suspensión:</strong> $endDate</p>
            <p>Si consideras que esto es un error, contacta al soporte.</p>
            """.trimIndent(),
            true
        )

        mailSender.send(message)
    }

    fun sendReactivationEmail(toEmail: String, username: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromAddress)
        helper.setTo(toEmail)
        helper.setSubject("Tu cuenta ha sido reactivada - Patrónika")
        helper.setText(
            """
            <h2>Hola, $username</h2>
            <p>Tu suspensión ha finalizado y tu cuenta en Patrónika ya está activa nuevamente.</p>
            <p>Ya puedes volver a iniciar sesión y usar la plataforma con normalidad.</p>
            <p>Gracias por ser parte de la comunidad.</p>
            """.trimIndent(),
            true
        )

        mailSender.send(message)
    }

    fun sendEmailChangeCode(toEmail: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromAddress)
        helper.setTo(toEmail)
        helper.setSubject("Verifica tu nuevo correo - Patronika")
        helper.setText(
            """
            <h2>Verificación de nuevo correo</h2>
            <p>Tu código para confirmar el cambio de correo es:</p>
            <h1 style="letter-spacing: 8px;">$code</h1>
            <p>Este código expira en <strong>10 minutos</strong>.</p>
            <p>Si no solicitaste esto, ignora este correo.</p>
            """.trimIndent(),
            true
        )

        mailSender.send(message)
    }

    fun sendVerificationCode(toEmail: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromAddress)
        helper.setTo(toEmail)
        helper.setSubject("Tu código de verificación - Patrónika")
        helper.setText(
            """
            <h2>Verificación de correo</h2>
            <p>Tu código de verificación es:</p>
            <h1 style="letter-spacing: 8px;">$code</h1>
            <p>Este código expira en <strong>10 minutos</strong>.</p>
            <p>Si no solicitaste esto, ignora este correo.</p>
            """.trimIndent(),
            true
        )

        mailSender.send(message)
    }
}