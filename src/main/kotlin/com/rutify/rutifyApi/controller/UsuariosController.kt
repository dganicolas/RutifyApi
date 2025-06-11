package com.rutify.rutifyApi.controller

import com.google.cloud.firestore.Firestore
import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.dto.*
import com.rutify.rutifyApi.service.UsuariosService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/usuarios")
class UsuariosController(
    private val db: Firestore,
    private val usuariosService: UsuariosService,
) {

    //documentado
    @PostMapping("/registrarse")
    fun registrarUsuario(@RequestBody usuario: UsuarioRegistroDTO): ResponseEntity<UsuarioregistradoDto> {
        return usuariosService.registrarUsuario(usuario)
    }

    //documentado
    @PostMapping("/acceder")
    fun loginUsuario(@RequestBody login: UsuarioCredencialesDto): ResponseEntity<UsuarioLoginDto> {
        return usuariosService.loginUsuarios(login)
    }
    //documentado
    @DeleteMapping("/eliminar")
    fun eliminarCuenta(
        @RequestBody eliminarUsuarioDTO: EliminarUsuarioDTO,
        authentication: Authentication,
    ): ResponseEntity<Unit> {
        return usuariosService.eliminarUsuarioPorCorreo(eliminarUsuarioDTO.correo, authentication)
    }
    //documentado
    @GetMapping("/buscar/{nombre}/{pagina}/{tamano}")
    fun buscarUsuariosPorNombre(
        @PathVariable nombre: String,
        @PathVariable pagina: Int,
        @PathVariable tamano: Int,
    ): ResponseEntity<List<UsuarioBusquedaDto>> {
        return usuariosService.buscarUsuariosPorNombre(nombre, pagina, tamano)
    }
    //documentado
    @GetMapping("/detalle/{idFirebase}")
    fun obtenerDetalleUsuario(
        @PathVariable idFirebase: String,
        authentication: Authentication,
    ): ResponseEntity<UsuarioInformacionDto> {
        return usuariosService.obtenerDetalleUsuario(idFirebase, authentication)
    }
    //documentado
    @PutMapping("/actualizar")
    fun actualizarCuenta(
        authentication: Authentication,
        @RequestBody actualizarUsuarioDTO: ActualizarUsuarioDTO,
    ): ResponseEntity<ActualizarUsuarioDTO> {
        return usuariosService.actualizarCuenta(authentication, actualizarUsuarioDTO)
    }
    //documentado
    @GetMapping("/esAdmin/{idFirebase}")
    fun EsAdmin(@PathVariable idFirebase: String): ResponseEntity<Boolean> {
        return usuariosService.EsAdmin(idFirebase)
    }
    //documentado
    @PostMapping("/reto-diario")
    fun retoDiario(authentication: Authentication): ResponseEntity<Boolean> {
        return usuariosService.marcarRetoDiario(authentication)
    }
    //docemntado
    @PutMapping("/avatar/cosmetico")
    fun aplicarCosmetico(
        authentication: Authentication,
        @RequestBody dto: Cosmetico,
    ): ResponseEntity<Unit> {
        return usuariosService.aplicarCosmetico(authentication, dto)
    }


}