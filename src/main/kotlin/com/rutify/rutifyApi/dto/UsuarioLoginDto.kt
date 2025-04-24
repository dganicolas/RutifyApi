package com.rutify.rutifyApi.dto

import org.springframework.data.mongodb.core.mapping.Field

data class UsuarioLoginDto(
    var nombre: String,
    var token:String
)