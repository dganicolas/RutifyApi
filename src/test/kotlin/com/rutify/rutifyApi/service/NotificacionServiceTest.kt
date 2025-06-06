package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.repository.IUsuarioRepository
import io.mockk.*
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import kotlin.test.Test

class NotificacionServiceTest {

    private val usuarioRepository = mockk<IUsuarioRepository>()
    private val emailService = mockk<EmailService>(relaxed = true)

    private val notificacionService = NotificacionService(usuarioRepository, emailService)

    @Test
    fun `incumplimiento - caso bueno - usuario existe y se envía correo`() {
        val idFirebase = "firebase123"
        val asunto = "Alerta"
        val cuerpo = "Tu contenido ha sido eliminado"
        val usuario = Usuario(
            idFirebase = idFirebase,
            nombre = "Nombre1",
            sexo = "M",
            esPremium = true,
            avatar = "avatar1",
            reportes = 2,
            gimnasioId = ObjectId(),
            fechaUltimoReto = LocalDate.now(),
            fechaNacimiento = LocalDate.now(),
            rol = "",
            correo = ""
        )

        every { usuarioRepository.findByIdFirebase(idFirebase) } returns usuario
        every { usuarioRepository.save(usuario) } returns usuario
        every { emailService.enviarCorreoNotificacion(usuario.correo, asunto, cuerpo) } just Runs

        notificacionService.incumplimiento(idFirebase, asunto, cuerpo)

        assertEquals(3, usuario.reportes)
        verify { usuarioRepository.findByIdFirebase(idFirebase) }
        verify { emailService.enviarCorreoNotificacion(usuario.correo, asunto, cuerpo) }
        verify { usuarioRepository.save(usuario) }
    }

    @Test
    fun `incumplimiento - caso malo - usuario no encontrado`() {
        val idFirebase = "no-existe"
        val asunto = "Advertencia"
        val cuerpo = "Mensaje de advertencia"

        every { usuarioRepository.findByIdFirebase(idFirebase) } returns null

        notificacionService.incumplimiento(idFirebase, asunto, cuerpo)

        verify { usuarioRepository.findByIdFirebase(idFirebase) }
        verify(exactly = 0) { emailService.enviarCorreoNotificacion(any(), any(), any()) }
        verify(exactly = 0) { usuarioRepository.save(any()) }
    }
}