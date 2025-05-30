package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.controller.CloudinaryService
import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.iService.IComunidadService
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ComunidadService(
    private val comentarioRepository: ComentarioRepository,
    private val cloudinaryService: CloudinaryService,
    usuariosRepository: IUsuarioRepository,
) : ServiceBase(usuariosRepository), IComunidadService {


    override fun crearComentario(comentarioDto: ComentarioDto, imagen: MultipartFile?): ComentarioDto {
        val imagenUrl: String? = if (imagen != null && !imagen.isEmpty) {
            cloudinaryService.subirImagen(imagen)
        } else {
            comentarioDto.imagenUrl
        }
        return DTOMapper.ComentarioToComentarioDto(
            comentarioRepository.save(
                Comentario(
                    idFirebase = comentarioDto.idFirebase,
                    nombreUsuario = comentarioDto.nombreUsuario,
                    avatarUrl = comentarioDto.avatarUrl,
                    fechaPublicacion = comentarioDto.fechaPublicacion,
                    imagenUrl = imagenUrl,
                    estadoAnimo = comentarioDto.estadoAnimo,
                    texto = comentarioDto.texto,
                    idComentarioPadre = null
                )
            )
        )
    }

    override fun obtenerComentarios(): List<ComentarioDto> {
        return comentarioRepository.findByIdComentarioPadreIsNull()
            .map { DTOMapper.ComentarioToComentarioDto(it) }.reversed()
    }

    override fun obtenerRespuestas(idComentarioPadre: String): List<ComentarioDto> {
        val comentarioPadre = comentarioRepository.findById(idComentarioPadre)
            .orElseThrow { NotFoundException("este comentario no existe") }

        val respuestas = comentarioRepository.findByIdComentarioPadre(idComentarioPadre)

        return buildList {
            add(DTOMapper.ComentarioToComentarioDto(comentarioPadre))
            addAll(respuestas.map { DTOMapper.ComentarioToComentarioDto(it) })
        }
    }

    override fun responderComentario(respuestaDto: ComentarioDto): ComentarioDto {
        return DTOMapper.ComentarioToComentarioDto(
            comentarioRepository.save(
                Comentario(
                    idFirebase = respuestaDto.idFirebase,
                    nombreUsuario = respuestaDto.nombreUsuario,
                    avatarUrl = respuestaDto.avatarUrl,
                    fechaPublicacion = respuestaDto.fechaPublicacion,
                    estadoAnimo = respuestaDto.estadoAnimo,
                    texto = respuestaDto.texto,
                    idComentarioPadre = respuestaDto.idComentarioPadre
                )
            )
        )
    }

    override fun eliminarComentario(id: String, authentication: Authentication) {
        val comentario = comentarioRepository.findById(id)
            .orElseThrow { NotFoundException("Comentario no encontrado") }

        val usuario = obtenerUsuario(authentication.name)

        val esAdmin = usuario.rol == "admin"

        val esAutorDelPadre = comentario.idComentarioPadre?.let { idPadre ->
            val comentarioPadre = comentarioRepository.findById(idPadre).orElse(null)
            comentarioPadre?.idFirebase == authentication.name
        } ?: false

        if (esAutorDelPadre && comentario.idFirebase != authentication.name && !esAdmin) {
            throw UnauthorizedException("No tienes autorización para eliminar este comentario")
        }

        comentarioRepository.deleteById(id)
    }
}
