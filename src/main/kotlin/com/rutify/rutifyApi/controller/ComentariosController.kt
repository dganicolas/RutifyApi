package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.iService.IComentariosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/comentarios")
class ComentariosController {

    @Autowired
    private lateinit var comentariosService: IComentariosService
    //documentado
    @PostMapping("/comentarios")
    fun crearComentario(
        @RequestPart("comentario") comentario: ComentarioDto,
        @RequestPart(value = "imagen", required = false) imagen: MultipartFile?,
    ): ResponseEntity<ComentarioDto> {
        val nuevoComentario = comentariosService.crearComentario(comentario, imagen)
        return ResponseEntity.ok(nuevoComentario)
    }
    //documentado
    @GetMapping("/comentarios")
    fun obtenerComentarios(): ResponseEntity<List<ComentarioDto>> {
        val comentarios = comentariosService.obtenerComentarios()
        return ResponseEntity.ok(comentarios)
    }
    //documentado
    @GetMapping("/comentarios/{id}/respuestas")
    fun obtenerRespuestas(@PathVariable id: String): ResponseEntity<List<ComentarioDto>> {
        val respuestas = comentariosService.obtenerRespuestas(id)
        return ResponseEntity.ok(respuestas)
    }
    //documentado
    @PostMapping("/comentarios/respuestas")
    fun responderComentario(
        @RequestBody respuesta: ComentarioDto,
    ): ResponseEntity<ComentarioDto> {
        val nuevaRespuesta = comentariosService.responderComentario(respuesta)
        return ResponseEntity.ok(nuevaRespuesta)
    }
    //documentado
    @DeleteMapping("/comentarios/{idComentario}")
    fun eliminarComentario(
        @PathVariable idComentario: String,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        comentariosService.eliminarComentario(idComentario, authentication)
        return ResponseEntity.noContent().build()
    }
    //documentaod
    @PutMapping("/comentarios/aprobar")
    fun aprobarComentario(
        @RequestBody comentario: ComentarioDto,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        comentariosService.aprobarComentario(comentario, authentication)
        return ResponseEntity.noContent().build()
    }
    //documentaod
    @GetMapping("/autor/{creadorId}")
    fun obtenerComentariosPorAutor(@PathVariable creadorId: String): ResponseEntity<List<ComentarioDto>> {
        return comentariosService.obtenerComentariosPorAutor(creadorId)
    }
    //documentado
    @GetMapping("/autorComentario/{nombre}")
    fun obtenerComentariosPorNombre(@PathVariable nombre: String): ResponseEntity<List<ComentarioDto>> {
        return comentariosService.obtenerComentariosPorNombre(nombre)
    }

}
