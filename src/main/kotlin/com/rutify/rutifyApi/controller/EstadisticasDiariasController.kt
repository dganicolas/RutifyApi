package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.dto.EstadisticasDiariasDto
import com.rutify.rutifyApi.dto.EstadisticasDiariasPatchDto
import com.rutify.rutifyApi.service.EstadisticasDiariasService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@RestController
@RequestMapping("/v1/estadisticasDiarias")
class EstadisticasDiariasController {

    @Autowired
    private lateinit var estadisticasDiariasService:  EstadisticasDiariasService

    @GetMapping("/ultimosPesos")
    fun obtenerUltimos5Pesos(
        @RequestParam idFirebase: String
    ): ResponseEntity<List<Double>> {
        return estadisticasDiariasService.obtenerUltimos5Pesos(idFirebase)
    }

    @PatchMapping
    fun actualizarEstadisticasDiarias(
        @RequestParam idFirebase: String,
        @RequestParam fecha: LocalDate,
        @RequestBody patch: EstadisticasDiariasPatchDto
    ): ResponseEntity<EstadisticasDiariasDto> {
        return estadisticasDiariasService.findByIdFirebaseAndFecha(idFirebase, fecha, patch)
    }

    @GetMapping("/dia")
    fun obtenerEstadisticasDiariasDia(
        @RequestParam idFirebase: String,
        @RequestParam fecha: LocalDate
    ): ResponseEntity<EstadisticasDiariasDto?> {
        return estadisticasDiariasService.obtenerEstadisticasDiariasDia(idFirebase,fecha)
    }
}