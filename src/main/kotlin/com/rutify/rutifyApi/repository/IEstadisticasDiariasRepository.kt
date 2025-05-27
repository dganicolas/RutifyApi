package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.EstadisticasDiarias
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate

interface IEstadisticasDiariasRepository : MongoRepository<EstadisticasDiarias, String> {
    fun findByIdFirebaseAndFecha(idFirebase: String, fecha: LocalDate): EstadisticasDiarias?

    fun findByIdFirebaseAndFechaBetween(
        idFirebase: String,
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): List<EstadisticasDiarias>

    fun findTop5ByIdFirebaseOrderByFechaDesc(idFirebase: String): List<EstadisticasDiarias>
}