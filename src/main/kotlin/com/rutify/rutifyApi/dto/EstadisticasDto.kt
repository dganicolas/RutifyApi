package com.rutify.rutifyApi.dto

data class EstadisticasDto(
    val lvlBrazo: Float,
    val lvlPecho: Float,
    val lvlEspalda: Float,
    val lvlPiernas: Float,
    val ejerciciosRealizados: Int,
    val caloriasQuemadas: Float
)