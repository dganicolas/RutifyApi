package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.EstadisticasDiarias
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate

interface IEstadisticasDiariasRepository : MongoRepository<EstadisticasDiarias, String> {
    fun findByIdFirebaseAndFecha(idFirebase: String, fecha: LocalDate): EstadisticasDiarias?
    fun findTop5ByIdFirebase(idFirebase: String): List<EstadisticasDiarias>
    fun deleteAllByIdFirebase(idFirebase: String)
    fun findTopByIdFirebaseAndFechaBeforeOrderByFechaDesc(idFirebase: String, fecha: LocalDate): EstadisticasDiarias?
}