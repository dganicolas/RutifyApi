package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.iService.IComunidadService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/comunidad")
class ComunidadController {

    @Autowired
    private lateinit var comunidadService: IComunidadService

    @PostMapping("/comentarios")
    fun crearComentario(
        @RequestPart("comentario") comentario: ComentarioDto,
        @RequestPart(value = "imagen", required = false) imagen: MultipartFile?,
    ): ResponseEntity<ComentarioDto> {
        val nuevoComentario = comunidadService.crearComentario(comentario, imagen)
        return ResponseEntity.ok(nuevoComentario)
    }

    @GetMapping("/comentarios")
    fun obtenerComentarios(): ResponseEntity<List<ComentarioDto>> {
        val comentarios = comunidadService.obtenerComentarios()
        return ResponseEntity.ok(comentarios)
    }

    @GetMapping("/comentarios/{id}/respuestas")
    fun obtenerRespuestas(@PathVariable id: String): ResponseEntity<List<ComentarioDto>> {
        val respuestas = comunidadService.obtenerRespuestas(id)
        return ResponseEntity.ok(respuestas)
    }

    @PostMapping("/comentarios/respuestas")
    fun responderComentario(
        @RequestBody respuesta: ComentarioDto,
    ): ResponseEntity<ComentarioDto> {
        val nuevaRespuesta = comunidadService.responderComentario(respuesta)
        return ResponseEntity.ok(nuevaRespuesta)
    }
}
