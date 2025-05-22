package com.rutify.rutifyApi.dto

data class RutinaDTO(
    val id: String? = null,
    val nombre: String,
    val imagen:String,
    val descripcion: String,
    val creadorId: String,
    val ejercicios: List<EjercicioDTO>,
    val equipo:String = "no especificado",
    val votos:Float = 0.0f,
    val totalVotos: Int = 0,
    val esPremium: Boolean
)