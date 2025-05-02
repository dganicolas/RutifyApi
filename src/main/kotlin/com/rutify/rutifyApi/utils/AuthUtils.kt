package com.rutify.rutifyApi.utils

import com.google.cloud.firestore.QuerySnapshot
import com.rutify.rutifyApi.domain.UsuarioFirebase
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException

object AuthUtils {
    fun verificarPermisos(
        usuarioFirebase: UsuarioFirebase,
        uidActual: String,
        mensajeError: String = "No tienes permisos para realizar esta acción."
    ) {
        val usuarioEsElMismo = usuarioFirebase.IdFirebase == uidActual
        val usuarioEsAdmin = usuarioFirebase.Rol == "Admin"

        if (!usuarioEsElMismo && !usuarioEsAdmin) {
            throw UnauthorizedException(mensajeError)
        }
    }
}