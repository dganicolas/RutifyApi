package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.repository.IUsuarioRepository
import org.springframework.stereotype.Service

@Service
class NotificacionService(
    private val usuarioRepository: IUsuarioRepository,
    private val emailService: EmailService
) {

    fun incumplimiento(idFirebase: String, asunto: String, cuerpo: String) {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)


        if (usuario != null) {
            usuario.reportes++
            emailService.enviarCorreoNotificacion(usuario.correo, asunto, cuerpo)
            usuarioRepository.save(usuario)
            println("Correo enviado a ${usuario.correo}")
        } else {
            println("Usuario con idFirebase $idFirebase no encontrado")
        }
    }
}
