package com.rutify.rutifyApi.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.iService.IComentariosService
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ComentarioService(
    private val comentarioRepository: ComentarioRepository,
    private val cloudinaryService: CloudinaryService,
    private val cloudinary: Cloudinary,
    private val mensajesService: MensajesService,
    usuariosRepository: IUsuarioRepository,
) : ServiceBase(usuariosRepository), IComentariosService {


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
                DTOMapper.ComnetarioDtoToComentario(respuestaDto)
            )
        )
    }
    fun extraerPublicId(imagenUrl: String?): String? {
        if (imagenUrl == null) return null
        val afterUpload = imagenUrl.substringAfter("/upload/").substringAfter("/")
        return afterUpload.substringBeforeLast(".")
    }

    override fun eliminarComentario(idComentario: String, authentication: Authentication) {

        val comentarioexistente = comentarioRepository.findById(idComentario)
            .orElseThrow { NotFoundException(mensajesService.obtenerMensaje("comentarioNoEncontrado")) }

        val esAutorDelPadre = comentarioexistente.idComentarioPadre?.let { idPadre ->
            val comentarioPadre = comentarioRepository.findById(idPadre).orElse(null)
            comentarioPadre?.idFirebase == authentication.name
        } ?: false

        if (esAutorDelPadre && comentarioexistente.idFirebase != authentication.name && obtenerUsuario(authentication.name).rol != "admin") {
            throw UnauthorizedException("No tienes autorización para eliminar este comentario")
        }

        val imagenUrl = extraerPublicId(comentarioexistente.imagenUrl)
        if (imagenUrl != null && imagenUrl.contains("res.cloudinary.com"))  cloudinary.uploader().destroy(imagenUrl, ObjectUtils.emptyMap())

        comentarioRepository.deleteByIdComentarioPadreEquals(comentarioexistente._id!!)
        comentarioRepository.delete(comentarioexistente)
    }

    override fun aprobarComentario(comentario:ComentarioDto, authentication: Authentication) {
        if (obtenerUsuario(authentication.name).rol != "admin") throw UnauthorizedException("No tienes permiso para aprobar comentarios")

        val comentarioexistente = comentarioRepository.findById(comentario._id!!)
            .orElseThrow { NotFoundException(mensajesService.obtenerMensaje("comentarioNoEncontrado")) }

        comentarioexistente.estado = null
        comentarioRepository.save(comentarioexistente)
    }

    override fun obtenerComentarioPorId(id:String): Comentario {
        return comentarioRepository.findById(id)
            .orElseThrow { NotFoundException(mensajesService.obtenerMensaje("comentarioNoEncontrado")) }
    }

    override fun obtenerComentariosPorAutor(idFirebase: String): ResponseEntity<List<ComentarioDto>> {
        if (idFirebase.isBlank()) {
            throw ValidationException("El ID del creador no puede estar vacío")
        }
        val comentarios = comentarioRepository.findAllByIdFirebaseAndIdComentarioPadreIsNull(idFirebase)
        return ResponseEntity.ok(comentarios.map { DTOMapper.ComentarioToComentarioDto(it) })
    }

    override fun obtenerComentariosPorNombre(nombre: String): ResponseEntity<List<ComentarioDto>> {
        if (nombre.isBlank()) {
            throw ValidationException("El nombre del creador no puede estar vacío")
        }
        val comentarios = comentarioRepository.findAllByNombreUsuarioContainingIgnoreCase(nombre)
        return ResponseEntity.ok(comentarios.map { DTOMapper.ComentarioToComentarioDto(it) })
    }
}
