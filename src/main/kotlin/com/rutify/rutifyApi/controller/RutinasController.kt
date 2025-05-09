package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Rutina
import com.rutify.rutifyApi.dto.RutinaBuscadorDto
import com.rutify.rutifyApi.dto.RutinaDTO
import com.rutify.rutifyApi.dto.RutinaPaginadaResponseDto
import com.rutify.rutifyApi.service.RutinaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/rutinas")
class RutinaController {

    @Autowired
    private lateinit var rutinaService: RutinaService

    @PostMapping("/crear")
    fun crearRutina(@RequestBody rutinaDTO: RutinaDTO): ResponseEntity<Rutina> {
        return rutinaService.crearRutina(rutinaDTO)
    }

    @GetMapping
    fun verRutinas(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) equipo: String?
    ): ResponseEntity<RutinaPaginadaResponseDto> {
        return rutinaService.obtenerRutinasBuscador(page, size, equipo)
    }

    @GetMapping("/{idRutina}")
    fun obtenerRutina(@PathVariable idRutina:String): ResponseEntity<RutinaDTO> {
        return rutinaService.obtenerRutinaPorId(idRutina)
    }
}
