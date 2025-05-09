package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface IUsuarioRepository : MongoRepository<Usuario, String> {

    fun findByIdFirebase(id:String):Usuario?
    fun findByCorreo(correo: String): Usuario?

    @Query("{ 'nombre': { \$regex: ?0, \$options: 'i' }, 'perfilPublico': true }")
    fun findByNombreContainsAndPerfilPublicoTrue(nombre: String, pageable: Pageable): Page<Usuario>

}