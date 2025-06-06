package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.dto.UsuarioBusquedaDto
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ModeracionService(
    private val comentarioRepository: ComentarioRepository,
    private val usuariosService: UsuariosService,
    private val comunidadService: ComentarioService,
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

    fun eliminarComentario(id: String, authentication: Authentication) {
        val comentario = comunidadService.obtenerComentarioPorId(id)
        if (!usuariosService.EsAdmin(authentication.name).body!!) throw UnauthorizedException("No tienes permiso")

        comunidadService.eliminarComentario(comentario._id!!, authentication)
        notificacionService.incumplimiento(
            comentario.idFirebase,
            mensajesService.obtenerMensaje("notificacion.titulo"),
            mensajesService.obtenerMensaje("notificacion.cuerpo")
        )
        println(mensajesService.obtenerMensaje("log.imagen.eliminada", arrayOf(comentario.imagenUrl)))
    }

    fun obtenerUsuariosReportados(authentication: Authentication): List<UsuarioBusquedaDto> {
        if (!usuariosService.EsAdmin(authentication.name).body!!) throw UnauthorizedException("No tienes permiso")
        return usuariosService.findByReportesGreaterThanOrderByReportesAsc().map{
            UsuarioBusquedaDto(
                idFirebase = it.idFirebase,
                nombre = it.nombre,
                sexo = it.sexo,
                esPremium = it.esPremium,
                avatar = it.avatar
            )
        }
    }

    fun eliminarUsuario(id: String, authentication: Authentication): ResponseEntity<Unit> {
        if (!usuariosService.EsAdmin(authentication.name).body!!) throw UnauthorizedException("No tienes permiso")
        val usuario = usuariosService.obtenerUsuario(id)

        return usuariosService.eliminarUsuarioPorCorreo(usuario.correo,authentication,false)
    }
}

