package com.rutify.rutifyApi.utils

import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException

object AuthUtils {
    fun verificarPermisos(
        usuario: Usuario,
        uidActual: String,
        mensajeError: String = "No tienes permisos para realizar esta acción."
    ) {
        val usuarioEsElMismo = usuario.idFirebase == uidActual
        val usuarioEsAdmin = usuario.rol == "Admin"

        if (!usuarioEsElMismo && !usuarioEsAdmin) {
            throw UnauthorizedException(mensajeError)
        }
    }
}