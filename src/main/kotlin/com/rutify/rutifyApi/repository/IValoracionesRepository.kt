package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Valoracion
import org.springframework.data.mongodb.repository.MongoRepository

interface IValoracionesRepository : MongoRepository<Valoracion, String> {
    fun findByIdRutina(idRutina: String): Valoracion?
}
