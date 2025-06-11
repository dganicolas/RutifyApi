package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Ejercicio
import com.rutify.rutifyApi.dto.EjercicioDTO
import com.rutify.rutifyApi.service.EjerciciosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/ejercicios")
class EjerciciosController {

    @Autowired
    private lateinit var ejerciciosService: EjerciciosService
    //documentado
    @PostMapping("/crear")
    fun crearEjercicio(@RequestBody ejercicioDTO: EjercicioDTO): ResponseEntity<Ejercicio> {
        return ejerciciosService.crearEjercicio(ejercicioDTO)
    }
    //documentado
    @GetMapping("/retodiario")
    fun obtenerRetoDiario(): ResponseEntity<EjercicioDTO> {
        val ejercicios = ejerciciosService.obtenerRetoDiario()
        return ResponseEntity.ok(ejercicios)
    }
    //documentado
    @GetMapping("/obteneEjercicios")
    fun obtenerEjercicios(
        @RequestParam(required = false) grupoMuscular: String?,
        @RequestParam(required = false) equipo: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<List<EjercicioDTO>> {
        val ejercicios = ejerciciosService.obtenerEjercicios(grupoMuscular, equipo, page, size)
        return ResponseEntity.ok(ejercicios)
    }
}