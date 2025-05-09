package com.rutify.rutifyApi.dto

data class BusquedaUsuariosRespuesta(
    val usuarios: List<UsuarioBusquedaDto>,
    val hasNext: Boolean
)

