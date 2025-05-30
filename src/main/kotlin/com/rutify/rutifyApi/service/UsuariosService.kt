package com.rutify.rutifyApi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.FirebaseLoginResponse
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.*
import com.rutify.rutifyApi.exception.exceptions.FirebaseUnavailableException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEstadisticasRepository
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.AuthUtils
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.Period

@Service
class UsuariosService(
    usuarioRepository: IUsuarioRepository,
    val estadisticasRepository: IEstadisticasRepository,
    val emailService: EmailService,
    val rutinaRepository: IRutinasRepository,
    @Value("\${firebase.apikey}") val apiKey: String,
    val firebaseAuth: FirebaseAuth
) : ServiceBase(usuarioRepository) {

    fun registrarUsuario(usuario: UsuarioRegistroDTO): ResponseEntity<UsuarioregistradoDto> {
        return try {
            val error = validarUsuarioRegistro(usuario)
            if (error != null) {
                throw ValidationException(error)
            }
            val request = UserRecord.CreateRequest()
                .setEmail(usuario.correo)
                .setPassword(usuario.contrasena)
            val userRecord = getFirebaseAuthInstance().createUser(request)

            val nuevoUsuario = Usuario(
                idFirebase = userRecord.uid,
                nombre = usuario.nombre,
                fechaNacimiento = usuario.fechaNacimiento,
                sexo = usuario.sexo,
                correo = usuario.correo,
                gimnasioId = null,
                esPremium = false,
                rol = "user",
                fechaUltimoReto = LocalDate.now().minusDays(-1)
            )

            usuarioRepository.save(nuevoUsuario)
            ResponseEntity(DTOMapper.usuarioRegisterDTOToUsuarioProfileDto(nuevoUsuario), HttpStatus.OK)
        } catch (e: FirebaseAuthException) {
            e.printStackTrace()
            throw FirebaseUnavailableException("Error con Firebase: ${e.message}")
        }
    }

    fun validarUsuarioRegistro(usuario: UsuarioRegistroDTO): String? {

        if (usuario.nombre.isBlank()) {
            return "El nombre no puede estar vacío"
        }

        if (!usuario.correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())) {
            return "El correo no es valido"
        }

        if (usuario.contrasena.isBlank() ||
            usuario.contrasena.length < 6
        ) {
            return "La contraseña debe tener al menos 6 caracteres y contener al menos un número"
        }

        if (Period.between(usuario.fechaNacimiento, LocalDate.now()).years < 16) {
            return "La edad no puede ser menor a 16 años"
        }

        if (usuario.sexo != "H" && usuario.sexo != "M" && usuario.sexo != "O") {
            return "El sexo debe ser 'H' (hombre), 'M' (mujer) O 'O' (otro sexo)"
        }

        if (usuarioRepository.findByCorreo(usuario.correo) != null) {
            return "El correo ya está registrado"
        }
        return null
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

    fun eliminarUsuarioPorCorreo(correo: String, authentication: Authentication): ResponseEntity<Void> {
        val uidActual = authentication.name // UID desde el JWT

        // Buscar en Firebase Firestore por correo
        val usuario = usuarioRepository.findByCorreo(correo)
            ?: throw NotFoundException("Usuario con correo $correo no encontrado.")

        AuthUtils.verificarPermisos(usuario,uidActual)

        eliminarDeFirestore(usuario.idFirebase)
        eliminarDeMongoDb(correo)
        emailService.enviarCorreoNotificacion(usuario.correo,"Cuenta eliminada de rutify","su cuenta a sido eliminada el dia ${LocalDate.now()}, lamentamos que te hayas despedido de nosotro")
        return ResponseEntity.noContent().build()
    }

    fun eliminarDeFirestore(uidUsuarioABorrar: String) {
        val autorizacion = getFirebaseAuthInstance()
        autorizacion.revokeRefreshTokens(uidUsuarioABorrar)
        autorizacion.deleteUser(uidUsuarioABorrar)
    }

    fun eliminarDeMongoDb(correo: String) {
        val usuario = usuarioRepository.findByCorreo(correo)
        if (usuario != null) {
            usuarioRepository.delete(usuario)
        } else {
            throw NotFoundException("Usuario con correo $correo no encontrado.")
        }
    }

    fun obtenerDetalleUsuario(idFirebase: String, authentication: Authentication): ResponseEntity<UsuarioInformacionDto> {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)
            ?: throw NotFoundException("Usuario no encontrado")
        val estadisticas = estadisticasRepository.findByIdFirebase(idFirebase) ?:
        Estadisticas(null,"",0.0, 0.0,0.0 , 0.0, 0.0, 0.0,0.0, 0, 0.0)
        val totalRutinas = rutinaRepository.countByCreadorId(idFirebase)
        if (usuario.perfilPublico || usuario.idFirebase == authentication.name) {
            return ResponseEntity.ok(
                UsuarioInformacionDto(
                    idFirebase = usuario.idFirebase,
                    nombre = usuario.nombre,
                    correo = usuario.correo,
                    sexo = usuario.sexo,
                    esPremium = usuario.esPremium,
                    avatarUrl = usuario.avatar,
                    estadisticas = DTOMapper.estadisticasToEstadisticasDto(estadisticas),
                    countRutinas = totalRutinas,
                    fechaUltimoReto = usuario.fechaUltimoReto
                )
            )
        }else{
            throw UnauthorizedException("Este usuario tiene el perfil en privado")
        }

    }

    fun buscarUsuariosPorNombre(nombre: String, pagina: Int, tamano: Int): ResponseEntity<BusquedaUsuariosRespuesta> {
        val pageable: Pageable = PageRequest.of(pagina, tamano)
        val paginaUsuarios = usuarioRepository.findByNombreContainsAndPerfilPublicoTrue(nombre, pageable)

        val usuarios = paginaUsuarios.content.map {
            UsuarioBusquedaDto(
                idFirebase = it.idFirebase,
                nombre = it.nombre,
                sexo = it.sexo,
                esPremium = it.esPremium,
                avatar = it.avatar
            )
        }

        return ResponseEntity.ok(
            BusquedaUsuariosRespuesta(
                usuarios = usuarios,
                hasNext = paginaUsuarios.hasNext()
            )
        )
    }


    fun actualizarCuenta(authentication: Authentication, actualizarUsuarioDTO: ActualizarUsuarioDTO):ResponseEntity<ActualizarUsuarioDTO> {

        val usuarioSolicitante = usuarioRepository.findByIdFirebase(authentication.name)
            ?: throw NotFoundException("Usuario solicitante no encontrado")
        // Buscar el usuario por correo
        val usuarioACambiar = usuarioRepository.findByCorreo(actualizarUsuarioDTO.correo)
            ?: throw NotFoundException("Usuario no encontrado")

        if (usuarioSolicitante.rol == "admin" || usuarioACambiar.correo != actualizarUsuarioDTO.correo) {
            throw UnauthorizedException("No tienes permiso para actualizar este perfil.")
        }
        val error = validarActualizarUsuarioDTO(actualizarUsuarioDTO)

        if (error != null) {
            throw ValidationException(error)
        }


        usuarioACambiar.nombre = actualizarUsuarioDTO.nombre ?: usuarioACambiar.nombre
        usuarioACambiar.sexo = actualizarUsuarioDTO.sexo ?: usuarioACambiar.sexo
        usuarioACambiar.fechaNacimiento = actualizarUsuarioDTO.fechaNacimiento ?: usuarioACambiar.fechaNacimiento
        usuarioACambiar.perfilPublico = actualizarUsuarioDTO.perfilPublico ?: usuarioACambiar.perfilPublico
        usuarioACambiar.avatar = actualizarUsuarioDTO.avatar ?: usuarioACambiar.avatar

        // Guardar el usuario actualizado
        usuarioRepository.save(usuarioACambiar)

        return ResponseEntity.ok(actualizarUsuarioDTO)
    }

    private fun validarActualizarUsuarioDTO(actualizarUsuarioDTO: ActualizarUsuarioDTO): String? {
        actualizarUsuarioDTO.nombre?.let {
            if (it.isBlank()) return "El nombre no puede estar vacío"
        }

        actualizarUsuarioDTO.fechaNacimiento?.let {
            if (Period.between(it, LocalDate.now()).years < 16) return "La edad no puede ser negativa"
        }

        actualizarUsuarioDTO.sexo?.let {
            if (it != "H" && it != "M") return "El sexo debe ser 'H' (hombre) o 'M' (mujer')"
        }

        return null
    }

    fun EsAdmin(idFirebase: String): ResponseEntity<Boolean> {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)
            ?: throw NotFoundException("Usuario no encontrado")

        return ResponseEntity.ok(usuario.rol == "admin")
    }

    fun marcarRetoDiario(authentication: Authentication): ResponseEntity<Boolean> {
        val idFirebase = authentication.name
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)?: throw NotFoundException("Usuario no encontrado")

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

}