package com.rutify.rutifyApi.dto

data class UsuarioBusquedaDto(
    val idFirebase: String,
    val nombre: String,
    val sexo: String,
    val esPremium: Boolean,
    val avatar: String,
)