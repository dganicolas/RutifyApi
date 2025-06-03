package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Estadisticas
import org.springframework.data.mongodb.repository.MongoRepository

interface IEstadisticasRepository : MongoRepository<Estadisticas, String> {

    fun findByIdFirebase(id:String): Estadisticas?
}