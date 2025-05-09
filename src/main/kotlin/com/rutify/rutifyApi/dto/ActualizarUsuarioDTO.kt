package com.rutify.rutifyApi.dto

import java.time.LocalDate

data class ActualizarUsuarioDTO(
    val correo: String,
    val nombre: String?,
    val sexo: String?,
    val fechaNacimiento: LocalDate?,
    val perfilPublico: Boolean?,
    val avatar: String?
)