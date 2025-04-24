package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.Usuario
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface IEstadisticasRepository : MongoRepository<Estadisticas, String> {

    fun findByIdFirebase(id:String): Estadisticas?
}