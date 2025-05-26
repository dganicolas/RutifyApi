package com.rutify.rutifyApi.controller

import com.google.cloud.firestore.Firestore
import com.rutify.rutifyApi.dto.*
import com.rutify.rutifyApi.service.UsuariosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/usuarios")
class UsuariosController(private val db: Firestore) {

    @Autowired
    private lateinit var usuariosService: UsuariosService

    @PostMapping("/registrarse")
    fun registrarUsuario(@RequestBody usuario: UsuarioRegistroDTO): ResponseEntity<UsuarioregistradoDto> {
        return usuariosService.registrarUsuario(usuario)
    }


    @PostMapping("/acceder")
    fun loginUsuario(@RequestBody login: UsuarioCredencialesDto): ResponseEntity<UsuarioLoginDto> {
       return usuariosService.loginUsuarios(login)
    }

    @DeleteMapping("/eliminar")
    fun eliminarCuenta(
        @RequestBody eliminarUsuarioDTO: EliminarUsuarioDTO,
        authentication: Authentication
    ): ResponseEntity<Void> {
        return usuariosService.eliminarUsuarioPorCorreo(eliminarUsuarioDTO.correo, authentication)
    }

    @GetMapping("/buscar/{nombre}")
    fun buscarUsuariosPorNombre(
        @RequestParam nombre: String,
        @RequestParam pagina: Int,
        @RequestParam tamaño: Int
    ): ResponseEntity<BusquedaUsuariosRespuesta> {
        return usuariosService.buscarUsuariosPorNombre(nombre, pagina, tamaño)
    }

    @GetMapping("/detalle/{idFirebase}")
    fun obtenerDetalleUsuario(@PathVariable idFirebase: String,authentication: Authentication ): ResponseEntity<UsuarioInformacionDto> {
        println(idFirebase)
        println(authentication.name)
        return usuariosService.obtenerDetalleUsuario(idFirebase,authentication)
    }

    @PutMapping("/actualizar")
    fun actualizarCorreo(
        authentication: Authentication,
        @RequestBody actualizarUsuarioDTO: ActualizarUsuarioDTO
    ): ResponseEntity<ActualizarUsuarioDTO> {
        return usuariosService.actualizarCuenta(authentication,actualizarUsuarioDTO)
    }

    @GetMapping("/esAdmin/{idFirebase}")
    fun EsAdmin(@PathVariable idFirebase: String): ResponseEntity<Boolean> {
        return usuariosService.EsAdmin(idFirebase)
    }
}