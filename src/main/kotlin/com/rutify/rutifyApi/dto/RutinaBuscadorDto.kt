package com.rutify.rutifyApi.dto

data class RutinaBuscadorDto(
    val id: String? = null,
    val nombre: String,
    val imagen:String,
    val descripcion: String,
    val cuantosEjercicios: Int,
    val votos:Float,
    val totalVotos: Int,
    val esPremium: Boolean,
    val equipo:String,
)
