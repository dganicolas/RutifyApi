package com.rutify.rutifyApi.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.rutify.rutifyApi.controller.CloudinaryService
import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.iService.IComunidadService
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ComunidadService(
    private val comentarioRepository: ComentarioRepository,
    private val cloudinaryService: CloudinaryService,
    private val cloudinary: Cloudinary,
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
                    idComentarioPadre = null,
                    estado = if(imagenUrl == null) null else false
                )
            )
        )
    }

    override fun obtenerComentarios(): List<ComentarioDto> {
        //comprobar si la imagen tienen estado null es ecir no comprbad
        return comentarioRepository.findByIdComentarioPadreIsNullAndEstadoIsNull()
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
    fun extraerPublicId(imagenUrl: String?): String? {
        if (imagenUrl == null) return null
        val afterUpload = imagenUrl.substringAfter("/upload/").substringAfter("/")
        return afterUpload.substringBeforeLast(".")
    }

    override fun eliminarComentario(comentario:ComentarioDto, authentication: Authentication) {
        val imagenUrl = extraerPublicId(comentario.imagenUrl)
        val comentarioexistente = comentarioRepository.findById(comentario._id!!)
            .orElseThrow { NotFoundException("Comentario no encontrado") }

        val usuario = obtenerUsuario(authentication.name)

        val esAdmin = usuario.rol == "admin"

        val esAutorDelPadre = comentarioexistente.idComentarioPadre?.let { idPadre ->
            val comentarioPadre = comentarioRepository.findById(idPadre).orElse(null)
            comentarioPadre?.idFirebase == authentication.name
        } ?: false

        if (esAutorDelPadre && comentarioexistente.idFirebase != authentication.name && !esAdmin) {
            throw UnauthorizedException("No tienes autorización para eliminar este comentario")
        }
        if (imagenUrl != null && imagenUrl.contains("res.cloudinary.com"))  cloudinary.uploader().destroy(imagenUrl, ObjectUtils.emptyMap())
        comentarioRepository.deleteByIdComentarioPadreEquals(comentarioexistente._id!!)
        comentarioRepository.delete(comentarioexistente)
    }

    override fun aprobarComentario(comentario:ComentarioDto, authentication: Authentication) {
        val usuario = obtenerUsuario(authentication.name)
        if (usuario.rol != "admin") throw UnauthorizedException("No tienes permiso para aprobar comentarios")


        val comentarioexistente = comentarioRepository.findById(comentario._id!!)
            .orElseThrow { NotFoundException("Comentario no encontrado") }

        comentarioexistente.estado = true
        comentarioRepository.save(comentarioexistente)
    }
}
