package com.rutify.rutifyApi.dto

import java.time.LocalDate

data class UsuarioInformacionDto(
    val idFirebase: String,
    val nombre: String,
    val correo: String,
    val sexo: String,
    val esPremium: Boolean,
    val avatarUrl: String,
    var fechaUltimoReto: LocalDate,
    val estadisticas: EstadisticasDto,
    val countRutinas: Long
)