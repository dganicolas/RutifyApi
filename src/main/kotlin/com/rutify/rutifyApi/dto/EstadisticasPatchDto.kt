package com.rutify.rutifyApi.dto

data class EstadisticasPatchDto(
    val lvlBrazo: Double? = null,
    val lvlPecho: Double? = null,
    val lvlEspalda: Double? = null,
    val lvlPiernas: Double? = null,
    val ejerciciosRealizados: Int? = null,
    val kCaloriasQuemadas: Double? = null,
    val lvlAbdominal: Double? = null
)
