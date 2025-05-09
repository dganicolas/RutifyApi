package com.rutify.rutifyApi.dto

import java.time.LocalDate


data class UsuarioRegistroDTO(
    var sexo: String,
    var fechaNacimiento: LocalDate,
    var nombre: String,
    var correo: String,
    var contrasena:String
)