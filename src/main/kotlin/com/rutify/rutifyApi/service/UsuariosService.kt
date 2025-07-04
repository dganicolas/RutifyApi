package com.rutify.rutifyApi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.domain.FirebaseLoginResponse
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.*
import com.rutify.rutifyApi.exception.exceptions.FirebaseUnavailableException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.*
import com.rutify.rutifyApi.utils.AuthUtils
import com.rutify.rutifyApi.utils.DTOMapper
import com.rutify.rutifyApi.utils.DTOMapper.usuarioRegistroDtoToUsuario
import com.rutify.rutifyApi.utils.DTOMapper.usuarioToUsuarioInformacionDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.Period

@Service
class UsuariosService(
    usuarioRepository: IUsuarioRepository,
    val estadisticasService: EstadisticasService,
    val emailService: EmailService,
    val rutinaRepository: IRutinasRepository,
    val comentarioService: ComentarioService,
    val votosService: VotosService,
    val estadisticasDiariasService: EstadisticasDiariasService,
    val compraRepository: CompraRepository,
    val reporteService: ReporteService,
    val rutinaService: RutinaService,
    val mensajesService: MensajesService,
    @Value("\${firebase.apikey}") val apiKey: String,
    val firebaseAuth: FirebaseAuth,
) : ServiceBase(usuarioRepository) {

    fun registrarUsuario(usuario: UsuarioRegistroDTO): ResponseEntity<UsuarioregistradoDto> {
        return try {
            validarUsuarioRegistro(usuario)
            val request = UserRecord.CreateRequest()
                .setEmail(usuario.correo)
                .setPassword(usuario.contrasena)
            val userRecord = firebaseAuth.createUser(request)
            val nuevoUsuario = usuarioRegistroDtoToUsuario(usuario, userRecord.uid)

            usuarioRepository.save(nuevoUsuario)
                emailService.enviarCorreoNotificacion(
                    usuario.correo,
                    "Cuenta creada en rutify",
                    "su cuenta a sido creada el dia ${LocalDate.now()}, Bienvenido a nuestra plataforma y mejora cada dia con nosotros"
                )
            ResponseEntity(DTOMapper.usuarioRegisterDTOToUsuarioProfileDto(nuevoUsuario), HttpStatus.OK)
        } catch (e: FirebaseAuthException) {
            e.printStackTrace()
            throw FirebaseUnavailableException("Error con Firebase: ${e.message}")
        }
    }

    fun validarUsuarioRegistro(usuario: UsuarioRegistroDTO) {

        if (usuario.nombre.isBlank()) {
            throw ValidationException("El nombre no puede estar vacío")
        }

        if (!usuario.correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())) {
            throw ValidationException("El correo no es valido")
        }

        if (usuario.contrasena.isBlank() ||
            usuario.contrasena.length < 6
        ) {
            throw ValidationException("La contraseña debe tener al menos 6 caracteres y contener al menos un número")
        }

        if (Period.between(usuario.fechaNacimiento, LocalDate.now()).years < 16) {
            throw ValidationException("La edad no puede ser menor a 16 años")
        }

        if (usuario.sexo != "H" && usuario.sexo != "M" && usuario.sexo != "O") {
            throw ValidationException("El sexo debe ser 'H' (hombre), 'M' (mujer) O 'O' (otro sexo)")
        }

        if (usuarioRepository.findByCorreo(usuario.correo) != null) {
            throw ValidationException("El correo ya está registrado")
        }
    }

    fun loginUsuarios(login: UsuarioCredencialesDto): ResponseEntity<UsuarioLoginDto> {

        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"

        val body = mapOf(
            "email" to login.correo,
            "password" to login.contrasena,
            "returnSecureToken" to true
        )

        val client = createHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(ObjectMapper().writeValueAsString(body)))
            .build()

        val token = client.send(request, HttpResponse.BodyHandlers.ofString())


        if (token.statusCode() == 200) {
            val firebaseResponse = ObjectMapper().readValue(token.body(), FirebaseLoginResponse::class.java)
            val usuario = usuarioRepository.findByIdFirebase(firebaseResponse.localId)

            return ResponseEntity.ok(UsuarioLoginDto(nombre = usuario!!.nombre, token = firebaseResponse.idToken))
        } else {
            val errorBody = token.body()
            throw UnauthorizedException("Credenciales incorrectas: $errorBody")
        }
    }

    fun eliminarUsuarioPorCorreo(correo: String, authentication: Authentication,estado: Boolean = true): ResponseEntity<Unit> {
        val uidActual = authentication.name

        val usuario = usuarioRepository.findByCorreo(correo)
            ?: throw NotFoundException("Usuario con correo $correo no encontrado.")

        AuthUtils.verificarPermisos(usuario, uidActual)


        votosService.eliminarVotosDeUnsuario(usuario.idFirebase,authentication)
        comentarioService.eliminarComentariosDeUnUsuario(usuario.idFirebase,authentication)
        compraRepository.deleteByIdUsuario(usuario.idFirebase)
        estadisticasService.deleteByIdUsuario(usuario.idFirebase,authentication)
        estadisticasDiariasService.eliminarEstadisticas(usuario.idFirebase,obtenerUsuario(authentication.name))
        reporteService.eliminarReportes(usuario.idFirebase,authentication)
        rutinaService.eliminarTodasRutinasDelusuario(usuario.idFirebase,authentication)
        eliminarDeFirestore(usuario.idFirebase)
        eliminarDeMongoDb(correo)
        if(estado){
            emailService.enviarCorreoNotificacion(
                usuario.correo,
                "Cuenta eliminada de rutify",
                "su cuenta a sido eliminada el dia ${LocalDate.now()}, lamentamos que te hayas despedido de nosotros"
            )
        }else{
            emailService.enviarCorreoNotificacion(
                usuario.correo,
                "Cuenta eliminada de rutify",
                "su cuenta a sido eliminada el dia ${LocalDate.now()}, por reportes negativos de usuarios"
            )
        }

        return ResponseEntity.noContent().build()
    }

    fun eliminarDeFirestore(uidUsuarioABorrar: String) {
        firebaseAuth.revokeRefreshTokens(uidUsuarioABorrar)
        firebaseAuth.deleteUser(uidUsuarioABorrar)
    }

    fun eliminarDeMongoDb(correo: String) {
        val usuario = usuarioRepository.findByCorreo(correo)
        if (usuario != null) {
            usuarioRepository.delete(usuario)
        } else {
            throw NotFoundException("Usuario con correo $correo no encontrado.")
        }
    }

    fun obtenerDetalleUsuario(
        idFirebase: String,
        authentication: Authentication,
    ): ResponseEntity<UsuarioInformacionDto> {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)
            ?: throw NotFoundException(mensajesService.obtenerMensaje("UsuarioNoEncontrado"))
        if (usuario.perfilPublico || usuario.idFirebase == authentication.name) {
            return ResponseEntity.ok(
                usuarioToUsuarioInformacionDto(
                    usuario, estadisticasService.findByIdFirebase(
                        idFirebase
                    ),
                    rutinaRepository.countByCreadorId(idFirebase),
                    comentarioService.countByIdFirebaseAndIdComentarioPadreIsNull(idFirebase),
                    votosService.countByIdFirebase(idFirebase)
                )

            )
        } else {
            throw UnauthorizedException("Este usuario tiene el perfil en privado")
        }

    }

    fun buscarUsuariosPorNombre(nombre: String, pagina: Int, tamano: Int): ResponseEntity<List<UsuarioBusquedaDto>> {
        return ResponseEntity.ok(
            usuarioRepository.findByNombreContainsAndPerfilPublicoTrue(
                nombre,
                PageRequest.of(pagina, tamano)
            ).content.map {
                UsuarioBusquedaDto(
                    idFirebase = it.idFirebase,
                    nombre = it.nombre,
                    sexo = it.sexo,
                    esPremium = it.esPremium,
                    avatar = it.avatar
                )
            })
    }

    fun anadirMonedas(idFirebase: String, coins: Int) {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)
            ?: throw NotFoundException(mensajesService.obtenerMensaje("UsuarioNoEncontrado"))
        usuario.monedas += coins
        usuarioRepository.save(usuario)
    }


    fun actualizarCuenta(
        authentication: Authentication,
        actualizarUsuarioDTO: ActualizarUsuarioDTO,
    ): ResponseEntity<ActualizarUsuarioDTO> {

        val usuarioSolicitante = usuarioRepository.findByIdFirebase(authentication.name)
            ?: throw NotFoundException("Usuario solicitante no encontrado")
        // Buscar el usuario por correo
        val usuarioACambiar = usuarioRepository.findByCorreo(actualizarUsuarioDTO.correo)
            ?: throw NotFoundException(mensajesService.obtenerMensaje("UsuarioNoEncontrado"))

        println("ROL del solicitante: ${usuarioSolicitante.rol}")
        println("CORREO del solicitante: ${usuarioSolicitante.correo}")
        println("CORREO del DTO a actualizar: ${actualizarUsuarioDTO.correo}")


        if (usuarioSolicitante.rol != "admin" && usuarioSolicitante.correo != actualizarUsuarioDTO.correo) {
            throw UnauthorizedException("No tienes permiso para actualizar este perfil.")
        }
        validarActualizarUsuarioDTO(actualizarUsuarioDTO)

        usuarioACambiar.nombre = actualizarUsuarioDTO.nombre ?: usuarioACambiar.nombre
        usuarioACambiar.sexo = actualizarUsuarioDTO.sexo ?: usuarioACambiar.sexo
        usuarioACambiar.fechaNacimiento = actualizarUsuarioDTO.fechaNacimiento ?: usuarioACambiar.fechaNacimiento
        usuarioACambiar.perfilPublico = actualizarUsuarioDTO.perfilPublico ?: usuarioACambiar.perfilPublico
        usuarioACambiar.avatar = actualizarUsuarioDTO.avatar ?: usuarioACambiar.avatar

        // Guardar el usuario actualizado
        usuarioRepository.save(usuarioACambiar)

        return ResponseEntity.ok(actualizarUsuarioDTO)
    }

    fun validarActualizarUsuarioDTO(actualizarUsuarioDTO: ActualizarUsuarioDTO) {
        actualizarUsuarioDTO.nombre?.let {
            if (it.isBlank()) throw ValidationException("El nombre no puede estar vacío")
        }
        actualizarUsuarioDTO.fechaNacimiento?.let {
            if (Period.between(
                    it,
                    LocalDate.now()
                ).years < 16
            ) throw ValidationException("La edad no puede ser negativa")
        }
        actualizarUsuarioDTO.sexo?.let {
            if (it != "H" && it != "M" && it != "O") throw ValidationException("El sexo debe ser 'O'(Otro) 'H' (hombre) o 'M' (mujer')")
        }
    }

    fun EsAdmin(idFirebase: String): ResponseEntity<Boolean> {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)
            ?: throw NotFoundException(mensajesService.obtenerMensaje("UsuarioNoEncontrado"))
        print(usuario.rol == "admin")
        return ResponseEntity.ok(usuario.rol == "admin")
    }

    fun marcarRetoDiario(authentication: Authentication): ResponseEntity<Boolean> {
        val usuario = usuarioRepository.findByIdFirebase(authentication.name)
            ?: throw NotFoundException(mensajesService.obtenerMensaje("UsuarioNoEncontrado"))
        val hoy = LocalDate.now()
        val fechaUltimoReto = usuario.fechaUltimoReto

        return if (fechaUltimoReto == hoy) {
            ResponseEntity.ok(true)
        } else {
            usuario.fechaUltimoReto = hoy
            usuarioRepository.save(usuario)
            ResponseEntity.ok(false)
        }
    }

    fun quitarMonedas(idFirebase: String, precioMonedas: Int) {
        val usuario = obtenerUsuario(idFirebase)

        if (usuario.monedas < precioMonedas) throw ValidationException("no hay monedas suficientes")
        usuario.monedas -= precioMonedas
        usuarioRepository.save(usuario)
    }

    fun aplicarCosmetico(authentication: Authentication, dto: Cosmetico): ResponseEntity<Unit> {
        val usuario = usuarioRepository.findByIdFirebase(authentication.name)
            ?: throw NotFoundException("Usuario no encontrado")

        when (dto.tipo) {
            "piel" -> usuario.indumentaria.colorPiel = dto.imagenUrl
            "camiseta" -> usuario.indumentaria.camiseta = dto.imagenUrl
            "pantalon" -> usuario.indumentaria.pantalon = dto.imagenUrl
            "tenis" -> usuario.indumentaria.tenis = dto.imagenUrl
            else -> throw ValidationException("Tipo de cosmético desconocido: ${dto.tipo}")
        }

        usuarioRepository.save(usuario)
        return ResponseEntity.ok().build()
    }

    fun findByReportesGreaterThanOrderByReportesAsc(): List<Usuario> {
        return usuarioRepository.findByReportesGreaterThanOrderByReportesAsc()
    }
}