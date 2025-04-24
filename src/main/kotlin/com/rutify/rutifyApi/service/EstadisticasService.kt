package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Estadisticas
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class EstadisticasService {
    fun crearEstadisticas(estadisticas: Estadisticas, authentication: Authentication): ResponseEntity<Estadisticas> {
        TODO("Not yet implemented")
        //crear la estadisticas si un usuario se la ha eliminado
        //comprobgar si tiene una estadisticas en caso afirmativo no crear nuevo registro
        //poner todo a 0
    }

    fun obtenerEstadisticasPorUsuarioId(usuarioId: String): ResponseEntity<Estadisticas> {
        /*
        * todos los usuario autenticados por que es una ruta protegida, puede buscar estadisticas de otras personas
        * revisar si el usuario tiene las estadisticas en privado en caso afirmativo, el usuario no podra ver esas estadisticas
        * */
        TODO("Not yet implemented")
    }

    fun actualizarEstadisticas(usuarioId: String, estadisticasActualizadas: Estadisticas, authentication: Authentication): ResponseEntity<Estadisticas> {
        TODO("Not yet implemented")
    }

    fun reiniciarEstadisticas(usuarioId: String, authentication: Authentication): ResponseEntity<Estadisticas> {
        TODO("Not yet implemented")
    }

    fun eliminarEstadisticasPorUsuarioId(usuarioId: String, authentication: Authentication): ResponseEntity<Void> {
        TODO("Not yet implemented")
    }
}