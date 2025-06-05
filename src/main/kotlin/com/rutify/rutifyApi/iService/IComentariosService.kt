package com.rutify.rutifyApi.iService

import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.dto.ComentarioDto
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.multipart.MultipartFile

interface IComentariosService {
    fun crearComentario(comentarioDto: ComentarioDto, imagen: MultipartFile?): ComentarioDto
    fun obtenerComentarios(): List<ComentarioDto>
    fun obtenerRespuestas(idComentarioPadre: String): List<ComentarioDto>
    fun responderComentario(respuestaDto: ComentarioDto): ComentarioDto
    fun eliminarComentario(idComentario: String, authentication: Authentication)
    fun aprobarComentario(comentario:ComentarioDto, authentication: Authentication)
    fun obtenerComentarioPorId(id:String): Comentario
    fun obtenerComentariosPorAutor(idFirebase:String): ResponseEntity<List<ComentarioDto>>
    fun obtenerComentariosPorNombre(nombre: String): ResponseEntity<List<ComentarioDto>>
    fun countByIdFirebaseAndIdComentarioPadreIsNull(idFirebase: String): Long
    fun eliminarComentariosDeUnUsuario(idFirebase: String, authentication: Authentication)

}
