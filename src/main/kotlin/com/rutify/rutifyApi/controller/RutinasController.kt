package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.dto.RutinaBuscadorDto
import com.rutify.rutifyApi.dto.RutinaDTO
import com.rutify.rutifyApi.service.RutinaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/rutinas")
class RutinaController {

    @Autowired
    private lateinit var rutinaService: RutinaService

    //documentado
    @PostMapping("/crear")
    fun crearRutina(@RequestBody rutinaDTO: RutinaDTO): ResponseEntity<RutinaDTO> {
        return rutinaService.crearRutina(rutinaDTO)
    }
    //documentado
    @GetMapping("/verRutinas")
    fun verRutinas(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) equipo: String?
    ): ResponseEntity<List<RutinaBuscadorDto>> {
        return rutinaService.obtenerRutinasBuscador(page, size, equipo)
    }
    //documentado
    @GetMapping("/buscarRutinas")
    fun buscarRutinas(
        @RequestParam(required = false) nombre: String?
    ): ResponseEntity<List<RutinaBuscadorDto>> {
        return rutinaService.buscarRutinas(nombre)
    }
    //documentada
    @GetMapping("/{idRutina}")
    fun obtenerRutina(@PathVariable idRutina:String): ResponseEntity<RutinaDTO> {
        return rutinaService.obtenerRutinaPorId(idRutina)
    }
    //documentada
    @GetMapping("/autor/{creadorId}")
    fun obtenerRutinasPorAutor(@PathVariable creadorId: String): ResponseEntity<List<RutinaBuscadorDto>> {
        return rutinaService.obtenerRutinasPorAutor(creadorId)
    }
    //documentado
    @DeleteMapping("/eliminar/{idRutina}")
    fun eliminarRutina(@PathVariable idRutina: String,authentication: Authentication): ResponseEntity<Unit> {
        return rutinaService.eliminarRutina(idRutina,authentication)
    }
}
