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
    fun registrarUsuario(@RequestBody usuario: UsuarioRegisterDTO): ResponseEntity<UsuarioregistradoDto> {
        return usuariosService.registrarUsuario(usuario)
    }


    @PostMapping("/acceder")
    fun loginUsuario(@RequestBody login: UsuarioCredencialesDto): ResponseEntity<UsuarioLoginDto> {
        return usuariosService.loginUsuarios(login)
    }

    @DeleteMapping("/eliminar/{correo}")
    fun eliminarCuenta(
        @PathVariable correo: String,
        authentication: Authentication
    ) {
        return usuariosService.eliminarUsuarioPorCorreo(correo, authentication)
    }

    @GetMapping("/buscar/usuarios/{nombre}")
    fun buscarUsuariosPorNombre(@PathVariable nombre: String): ResponseEntity<List<UsuarioBusquedaDto>> {
        return usuariosService.buscarUsuariosPorNombre(nombre)
    }

    @GetMapping("/detalle/{idFirebase}")
    fun obtenerDetalleUsuario(@PathVariable idFirebase: String): ResponseEntity<UsuarioInformacionDto> {
        return usuariosService.obtenerDetalleUsuario(idFirebase)
    }

    @PutMapping("/actualizar/{correo}")
    fun actualizarCorreo(
        @PathVariable correo: String,
        authentication: Authentication
    ) {

    }
}