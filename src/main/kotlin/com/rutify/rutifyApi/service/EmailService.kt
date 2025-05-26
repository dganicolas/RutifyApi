package com.rutify.rutifyApi.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(private val mailSender: JavaMailSender) {

    fun enviarCorreoNotificacion(destinatario: String, asunto: String, cuerpo: String) {
        val mensaje = SimpleMailMessage()
        mensaje.setTo(destinatario)
        mensaje.subject = asunto
        mensaje.text = cuerpo
        mailSender.send(mensaje)
    }
}