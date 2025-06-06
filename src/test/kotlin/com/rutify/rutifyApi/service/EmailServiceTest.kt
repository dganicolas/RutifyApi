package com.rutify.rutifyApi.service

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import kotlin.test.Test

class EmailServiceTest{
    private val mailSender = mockk<JavaMailSender>(relaxed = true)
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setUp() {
        emailService = EmailService(mailSender)
    }

    @Test
    fun `enviarCorreoNotificacion envía el correo correctamente`() {
        // Preparamos para que mailSender.send no lance excepción
        every { mailSender.send(any<SimpleMailMessage>()) } just Runs

        val destinatario = "usuario@example.com"
        val asunto = "Asunto de prueba"
        val cuerpo = "Cuerpo del mensaje"

        emailService.enviarCorreoNotificacion(destinatario, asunto, cuerpo)
    }
}