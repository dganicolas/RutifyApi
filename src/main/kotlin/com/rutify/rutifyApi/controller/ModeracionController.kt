package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.dto.UsuarioBusquedaDto
import com.rutify.rutifyApi.service.ModeracionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/moderacion")
class ModeracionController(val moderacionService: ModeracionService) {

    @GetMapping("/verificar")
    fun verificar(authentication: Authentication,): ResponseEntity<List<ComentarioDto>> {
        return ResponseEntity.ok(moderacionService.verificarModeracionImagenes(authentication))
    }

    @DeleteMapping("/eliminar/{id}")
    fun eliminarComentario(
        @PathVariable id: String,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        moderacionService.eliminarComentario(id, authentication)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/reportados")
    fun obtenerUsuariosReportados(authentication: Authentication): ResponseEntity<List<UsuarioBusquedaDto>> {
        return ResponseEntity.ok(moderacionService.obtenerUsuariosReportados(authentication))
    }

    @DeleteMapping("/usuario/{id}")
    fun obtenerUsuariosReportados(@PathVariable id: String,authentication: Authentication): ResponseEntity<ResponseEntity<Unit>> {
        return ResponseEntity.ok(moderacionService.eliminarUsuario(id,authentication))
    }
}
