package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Voto
import org.springframework.data.mongodb.repository.MongoRepository

interface IVotosRepository : MongoRepository<Voto, String> {
    fun findByIdFirebaseAndIdRutina(idFirebase: String, idRutina: String): Voto?
    fun findByIdRutina(idRutina: String): List<Voto>
}
