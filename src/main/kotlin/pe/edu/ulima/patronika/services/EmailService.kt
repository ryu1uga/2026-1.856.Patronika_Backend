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
    fun sendVerificationCode(toEmail: String, code: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(fromAddress)
        helper.setTo(toEmail)
        helper.setSubject("Tu código de verificación - Patronika")
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