package com.rutify.rutifyApi.iService

import com.rutify.rutifyApi.dto.ComentarioDto
import org.springframework.web.multipart.MultipartFile

interface IComunidadService {
    fun crearComentario(comentarioDto: ComentarioDto, imagen: MultipartFile?): ComentarioDto
    fun obtenerComentarios(): List<ComentarioDto>
    fun obtenerRespuestas(idComentarioPadre: String): List<ComentarioDto>
    fun responderComentario(respuestaDto: ComentarioDto): ComentarioDto
}
