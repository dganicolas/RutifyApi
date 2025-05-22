package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.dto.EstadisticasDto
import com.rutify.rutifyApi.dto.EstadisticasPatchDto
import com.rutify.rutifyApi.service.EstadisticasService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/estadisticas")
class EstadisticasController {
    @Autowired
    private lateinit var estadisticasService: EstadisticasService

    @PostMapping("/crear")
    fun crearEstadisticas(
        @RequestBody estadisticas: Estadisticas,
        authentication: Authentication,
    ): ResponseEntity<EstadisticasDto> {
        return estadisticasService.crearEstadisticas(estadisticas, authentication)
    }

    @GetMapping("/{usuarioId}")
    fun obtenerEstadisticas(@PathVariable usuarioId: String): ResponseEntity<EstadisticasDto> {
        return estadisticasService.obtenerEstadisticasPorUsuarioId(usuarioId)
    }

    @PatchMapping("/{usuarioId}")
    fun actualizarEstadisticas(
        @PathVariable usuarioId: String,
        @RequestBody estadisticasActualizadas: EstadisticasPatchDto,
        authentication: Authentication,
    ): ResponseEntity<EstadisticasDto> {
        return estadisticasService.actualizarEstadisticas(usuarioId, estadisticasActualizadas, authentication)
    }

    @PutMapping("/reiniciar/{usuarioId}")
    fun reiniciarEstadisticas(
        @PathVariable usuarioId: String,
        authentication: Authentication,
    ): ResponseEntity<EstadisticasDto> {
        return estadisticasService.reiniciarEstadisticas(usuarioId, authentication)
    }

    @DeleteMapping("/{usuarioId}")
    fun eliminarEstadisticas(
        @PathVariable usuarioId: String,
        authentication: Authentication,
    ): ResponseEntity<EstadisticasDto> {
        return estadisticasService.eliminarEstadisticasPorUsuarioId(usuarioId, authentication)
    }

}