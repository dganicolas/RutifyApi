package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.controller.CloudinaryService
import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.iService.IComunidadService
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ComunidadService() : IComunidadService {

    @Autowired
    private lateinit var comentarioRepository: ComentarioRepository
    @Autowired
    private lateinit var cloudinaryService : CloudinaryService

    override fun crearComentario(comentarioDto: ComentarioDto, imagen: MultipartFile?): ComentarioDto {
        val imagenUrl: String? = if (imagen != null && !imagen.isEmpty) {
            cloudinaryService.subirImagen(imagen)
        } else {
            comentarioDto.imagenUrl
        }
        return DTOMapper.ComentarioToComentarioDto(comentarioRepository.save(Comentario(
            idFirebase = comentarioDto.idFirebase,
            nombreUsuario = comentarioDto.nombreUsuario,
            avatarUrl = comentarioDto.avatarUrl,
            fechaPublicacion = comentarioDto.fechaPublicacion,
            imagenUrl = imagenUrl,
            estadoAnimo = comentarioDto.estadoAnimo,
            texto = comentarioDto.texto,
            idComentarioPadre = null
        )))
    }

    override fun obtenerComentarios(): List<ComentarioDto> {
        return comentarioRepository.findByIdComentarioPadreIsNull()
            .map { DTOMapper.ComentarioToComentarioDto(it) }.reversed()
    }

    override fun obtenerRespuestas(idComentarioPadre: String): List<ComentarioDto> {
        val comentarioPadre = comentarioRepository.findById(idComentarioPadre).orElseThrow{ NotFoundException("este comentario no existe") }

        val respuestas = comentarioRepository.findByIdComentarioPadre(idComentarioPadre)

        return buildList {
            add(DTOMapper.ComentarioToComentarioDto(comentarioPadre))
            addAll(respuestas.map { DTOMapper.ComentarioToComentarioDto(it) })
        }
    }

    override fun responderComentario(respuestaDto: ComentarioDto): ComentarioDto {
        return DTOMapper.ComentarioToComentarioDto(comentarioRepository.save(Comentario(
            idFirebase = respuestaDto.idFirebase,
            nombreUsuario = respuestaDto.nombreUsuario,
            avatarUrl = respuestaDto.avatarUrl,
            fechaPublicacion = respuestaDto.fechaPublicacion,
            estadoAnimo = respuestaDto.estadoAnimo,
            texto = respuestaDto.texto,
            idComentarioPadre = respuestaDto.idComentarioPadre
        )))
    }


}
