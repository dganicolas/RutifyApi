package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Compra
import org.springframework.data.mongodb.repository.MongoRepository

interface CompraRepository : MongoRepository<Compra, String> {
    fun findByIdUsuario(idUsuario: String): List<Compra>
    fun findByIdUsuarioAndIdCosmetico(idUsuario: String, idCosmetico: String): Compra?
}
