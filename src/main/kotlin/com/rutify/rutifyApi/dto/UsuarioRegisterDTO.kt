package com.rutify.rutifyApi.dto


data class UsuarioRegisterDTO(
    var sexo: String,
    var edad: Int,
    var nombre: String,
    var correo: String,
    var contrasena:String
)
