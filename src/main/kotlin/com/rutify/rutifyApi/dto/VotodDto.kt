package com.rutify.rutifyApi.dto

data class VotodDto(
    val id: String? = null,
    val idFirebase: String,
    val idRutina: String,
    var nombreRutina: String,
    val puntuacion: Float,
)
