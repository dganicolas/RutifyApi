package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Comentario
import org.springframework.data.mongodb.repository.MongoRepository

interface ComentarioRepository : MongoRepository<Comentario, String> {
    fun findByIdComentarioPadre(idComentarioPadre: String): List<Comentario>
    fun findByIdComentarioPadreIsNullAndEstadoIsNull(): List<Comentario>
    fun findByEstadoIsFalse(): List<Comentario>
    fun deleteByIdComentarioPadreEquals(id: String): Long
    fun countByIdFirebaseAndIdComentarioPadreIsNull(idUsuario: String): Long
    fun findAllByIdFirebaseAndIdComentarioPadreIsNull(idFirebase: String): List<Comentario>
    fun findAllByNombreUsuarioContainingIgnoreCase(nombre: String): List<Comentario>
}
