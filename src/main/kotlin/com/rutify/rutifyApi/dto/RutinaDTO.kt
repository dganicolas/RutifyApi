package com.rutify.rutifyApi.dto

import com.rutify.rutifyApi.domain.Ejercicio

data class RutinaDTO(
    val nombre: String,
    val descripcion: String,
    val creadorId: String,
    val ejercicios: List<Ejercicio>,
    val equipo:String = "no especificado",
    val esPremium: Boolean
)