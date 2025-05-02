package com.rutify.rutifyApi.dto

data class ActualizarUsuarioDTO(
    val nombre: String?,
    val sexo: String?,
    val edad: Int?,
    val perfilPublico: Boolean?,
    val avatar: String?
)