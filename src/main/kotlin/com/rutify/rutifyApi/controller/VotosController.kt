package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.dto.VotodDto
import com.rutify.rutifyApi.service.VotosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/votos")
class VotosController {

    @Autowired
    private lateinit var votosService: VotosService

    @PostMapping("/registrar")
    fun registrarVoto(@RequestBody voto: VotodDto, authentication: Authentication): ResponseEntity<VotodDto> {
        return votosService.agregarVotacion(voto, authentication)
    }

    @GetMapping("/obtenerVoto")
    fun obtenerVoto(@RequestParam idFirebase: String,
                    @RequestParam idRutina: String, authentication: Authentication): ResponseEntity<VotodDto> {
        return votosService.obtenerVoto(idFirebase,idRutina, authentication)
    }

    @PatchMapping("/actualizar")
    fun actualizarVoto(@RequestBody voto: VotodDto, authentication: Authentication): ResponseEntity<VotodDto> {
        return votosService.actualizarVotos(voto, authentication)
    }

    @DeleteMapping("eliminarVoto/{idVoto}")
    fun eliminarVoto(@PathVariable idVoto: String, authentication: Authentication): ResponseEntity<Unit> {
        return votosService.eliminarVoto(idVoto, authentication)
    }

    @GetMapping("/autor/{creadorId}")
    fun obtenerVotosPorAutor(@PathVariable creadorId: String): ResponseEntity<List<VotodDto>> {
        return votosService.obtenerComentariosPorAutor(creadorId)
    }
}