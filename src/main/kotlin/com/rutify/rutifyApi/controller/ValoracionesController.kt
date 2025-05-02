package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Valoracion
import com.rutify.rutifyApi.dto.VotoDto
import com.rutify.rutifyApi.service.ValoracionesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/valoraciones")
class ValoracionController {

    @Autowired
    private lateinit var valoracionService: ValoracionesService

    @PostMapping("/crear")
    fun crearValoracion(
        @RequestParam idFirebase: String,
        @RequestParam idRutina: String,
        @RequestParam puntuacion: Int
    ): ResponseEntity<VotoDto> {
        return valoracionService.crearVoto(idFirebase, idRutina, puntuacion)
    }

    @GetMapping("/obtener")
    fun obtenerValoraciones(@RequestParam idRutina: String): ResponseEntity<Valoracion> {
        return valoracionService.obtenerValoraciones(idRutina)
    }
}
