package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.EstadisticasDiarias
import com.rutify.rutifyApi.dto.EstadisticasDiariasDto
import com.rutify.rutifyApi.service.EstadisticasDiariasService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@RestController
@RequestMapping("/v1/estadisticasDiarias")
class EstadisticasDiariasController {

    @Autowired
    private lateinit var estadisticasDiariasService:  EstadisticasDiariasService
    @GetMapping("/mes")
    fun obtenerEstadisticasDiariasDeUnMes(
        @RequestParam idFirebase: String,
        @RequestParam fecha: LocalDate
    ): ResponseEntity<List<EstadisticasDiariasDto>> {
        return estadisticasDiariasService.obtenerEstadisticasDiariasDeUnMes(idFirebase,fecha)

    }

    // 1. Obtener estadísticas de un día concreto
    @GetMapping("/dia")
    fun obtenerEstadisticasDiariasDia(
        @RequestParam idFirebase: String,
        @RequestParam fecha: LocalDate
    ): ResponseEntity<EstadisticasDiariasDto?> {
        return estadisticasDiariasService.obtenerEstadisticasDiariasDia(idFirebase,fecha)
    }
}