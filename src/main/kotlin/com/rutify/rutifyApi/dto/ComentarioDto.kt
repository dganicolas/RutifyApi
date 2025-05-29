package com.rutify.rutifyApi.dto

import java.time.LocalDate

data class ComentarioDto(
    val _id:String? = null,
    val idFirebase: String,
    val nombreUsuario: String,
    val avatarUrl: String,
    val fechaPublicacion: LocalDate,
    val estadoAnimo: String,
    val imagenUrl: String? = null,
    val texto: String,
    val idComentarioPadre: String? = null // Para respuestas
)
