package com.rutify.rutifyApi.dto

import com.rutify.rutifyApi.domain.Ejercicio

data class RutinaDTO(
    val id: String? = null,
    val nombre: String,
    val imagen:String,
    val descripcion: String,
    val creadorId: String,
    val ejercicios: List<EjercicioDTO>,
    val equipo:String = "no especificado",
    val esPremium: Boolean
)