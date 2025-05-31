package com.rutify.rutifyApi.service

import com.cloudinary.Cloudinary
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class ModeracionService(
    private val comentarioRepository: ComentarioRepository,
    private val usuariosService: UsuariosService,
    private val comunidadService: ComunidadService,
    private val notificacionService: NotificacionService,
    private val mensajesService: MensajesService,
) {

    fun verificarModeracionImagenes(authentication: Authentication): List<ComentarioDto> {
        if (!usuariosService.EsAdmin(authentication.name).body!!) throw UnauthorizedException("No tienes permiso")
        val pendientes = comentarioRepository.findByEstadoIsFalse()
        return pendientes.map { comentario ->
            DTOMapper.ComentarioToComentarioDto(comentario)
        }
    }

    fun eliminarComentario(comentario: ComentarioDto, authentication: Authentication) {

        if (!usuariosService.EsAdmin(authentication.name).body!!) throw UnauthorizedException("No tienes permiso")

        comunidadService.eliminarComentario(comentario, authentication)
        notificacionService.incumplimiento(
            comentario.idFirebase,
            mensajesService.obtenerMensaje("notificacion.titulo"),
            mensajesService.obtenerMensaje("notificacion.cuerpo")
        )
        println(mensajesService.obtenerMensaje("log.imagen.eliminada", arrayOf(comentario.imagenUrl)))
    }
}

