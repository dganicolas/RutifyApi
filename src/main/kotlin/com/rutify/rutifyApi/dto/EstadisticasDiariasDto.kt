package com.rutify.rutifyApi.dto

import java.time.LocalDate

data class EstadisticasDiariasDto(
    val _id: String? = null,
    val idFirebase: String,
    val fecha: LocalDate,
    val horasActivos: Double,
    val pesoCorporal: Double,
    var ejerciciosRealizados: Int,
    var kCaloriasQuemadas: Double
)