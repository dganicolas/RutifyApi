package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.service.ModeracionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/moderacion")
class ModeracionController(val moderacionService: ModeracionService) {

    @GetMapping("/verificar")
    fun verificar(authentication: Authentication,): ResponseEntity<List<ComentarioDto>> {
        return ResponseEntity.ok(moderacionService.verificarModeracionImagenes(authentication))
    }

    @DeleteMapping("/eliminar")
    fun eliminarComentario(
        @RequestBody comentario: ComentarioDto,
        authentication: Authentication
    ): ResponseEntity<Void> {
        moderacionService.eliminarComentario(comentario, authentication)
        return ResponseEntity.noContent().build()
    }
}
