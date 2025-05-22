package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Rutina
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface IRutinasRepository : MongoRepository<Rutina, String>{
    override fun findById(id: String): Optional<Rutina>
    fun countByCreadorId(idFirebase: String): Long
}
