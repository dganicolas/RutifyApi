package com.rutify.rutifyApi.dto

data class EstadisticasDto(
    val idFirebase: String,
    val lvlBrazo: Double,
    val lvlAbdominal:Double,
    val lvlPecho: Double,
    val lvlEspalda: Double,
    val lvlPiernas: Double,
    val ejerciciosRealizados: Int,
    val kCaloriasQuemadas: Double
)