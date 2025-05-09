package com.rutify.rutifyApi.dto

data class EjercicioDTO(
    val id:String,
    val nombreEjercicio: String,
    val descripcion: String,
    val imagen: String,
    val equipo: String,
    val grupoMuscular: String,
    val caloriasQuemadasPorRepeticion: Double,
    val puntoGanadosPorRepeticion: Double,
    val cantidad: Int
)
