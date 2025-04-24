package com.rutify.rutifyApi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.FirebaseLoginResponse
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.domain.UsuarioFirebase
import com.rutify.rutifyApi.dto.*
import com.rutify.rutifyApi.exception.exceptions.FirebaseUnavailableException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEstadisticasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class UsuariosService {

    @Autowired
    lateinit var usuarioRepository: IUsuarioRepository
    @Autowired
    lateinit var estadisticasRepository: IEstadisticasRepository

    @Autowired
    lateinit var db: Firestore

    @Value("\${firebase.apikey}")
    lateinit var apiKey: String

    @Autowired
    lateinit var firebaseAuth: FirebaseAuth

    @Autowired
    lateinit var httpClient: HttpClient


    fun registrarUsuario(usuario: UsuarioRegisterDTO): ResponseEntity<UsuarioregistradoDto> {
        return try {
            val error = validarUsuarioRegistro(usuario)
            if (error != null) {
                throw ValidationException(error)
            }
            val request = UserRecord.CreateRequest()
                .setEmail(usuario.correo)
                .setPassword(usuario.contrasena)
            val userRecord = firebaseAuth.createUser(request)

            // Crear un documento de usuario en Firestore con su rol
            val usuarioFirebase = UsuarioFirebase(
                IdFirebase = userRecord.uid,
                Nombre = usuario.nombre,
                Email = usuario.correo,
                Rol = "user",
            )

            // Guardar el usuario en Firestore
            db.collection("Usuarios")
                .document(userRecord.uid)
                .set(usuarioFirebase)

            val nuevoUsuario = Usuario(
                idFirebase = userRecord.uid,
                nombre = usuario.nombre,
                edad = usuario.edad,
                sexo = usuario.sexo,
                correo = usuario.correo,
                gimnasioId = null,
                esPremium = false
            )

            usuarioRepository.save(nuevoUsuario)

            ResponseEntity(DTOMapper.usuarioRegisterDTOToUsuarioProfileDto(nuevoUsuario), HttpStatus.OK)
        } catch (e: FirebaseAuthException) {
            e.printStackTrace()
            throw FirebaseUnavailableException("Error con Firebase: ${e.message}")
        }
    }

    fun loginUsuarios(login: UsuarioCredencialesDto): ResponseEntity<UsuarioLoginDto> {

        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"

        val body = mapOf(
            "email" to login.correo,
            "password" to login.contrasena,
            "returnSecureToken" to true
        )

        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(ObjectMapper().writeValueAsString(body)))
            .build()

        val token = client.send(request, HttpResponse.BodyHandlers.ofString())

        val querySnapshot = db.collection("Usuarios")
            .whereEqualTo("email", login.correo)
            .get()
            .get()

        if (querySnapshot.isEmpty) {
            throw NotFoundException("Usuario no encontrado en Firestore")
        }

        val document = querySnapshot.documents[0]
        val nombre = document.getString("nombre") ?: "Usuario"

        if (token.statusCode() == 200) {
            val firebaseResponse = ObjectMapper().readValue(token.body(), FirebaseLoginResponse::class.java)
            return ResponseEntity.ok(UsuarioLoginDto(nombre = nombre, token = firebaseResponse.idToken))
        } else {
            val errorBody = token.body()
            throw UnauthorizedException("Credenciales incorrectas: $errorBody")
        }
    }

    fun eliminarUsuarioPorCorreo(correo: String, authentication: Authentication) {
        val uidActual = authentication.name // UID desde el JWT

        // Buscar en Firebase Firestore por correo
        val querySnapshot = db.collection("Usuarios")
            .whereEqualTo("email", correo)
            .get()
            .get()

        if (querySnapshot.isEmpty) {
            throw NotFoundException("Usuario con correo $correo no encontrado.")
        }

        val usuarioFirebase = querySnapshot.documents[0].toObject(UsuarioFirebase::class.java)
        val usuarioEsElMismo = usuarioFirebase.IdFirebase == uidActual
        val usuarioEsAdmin = (usuarioFirebase.Rol) == ("Admin")

        if (!usuarioEsElMismo && !usuarioEsAdmin) {
            throw UnauthorizedException("No tienes permisos para eliminar este usuario.")
        }

        eliminarDeFirestore(usuarioFirebase.IdFirebase)
        eliminarDeMongoDb(correo)
    }

    fun obtenerDetalleUsuario(idFirebase: String): ResponseEntity<UsuarioInformacionDto> {
        val usuario = usuarioRepository.findByIdFirebase(idFirebase)
            ?: throw NotFoundException("Usuario no encontrado")
        val estadisticas = estadisticasRepository.findByIdFirebase(idFirebase) ?:
        Estadisticas(null,"",0f, 0f, 0f, 0f, 0, 0f)

        if (!usuario.perfilPublico) {
            // Si el perfil es privado y el usuario autenticado no es el propio usuario
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                UsuarioInformacionDto(
                    idFirebase = usuario.idFirebase,
                    nombre = usuario.nombre,
                    correo = usuario.correo,
                    sexo = usuario.sexo,
                    esPremium = usuario.esPremium,
                    avatarUrl = usuario.avatar,
                    estadisticas = DTOMapper.estadisticasToEstadisticasDto(estadisticas)
                )
            )
        }else{
            throw UnauthorizedException("Este usuario tiene el perfil en privado")
        }

    }

    fun buscarUsuariosPorNombre(nombre: String): ResponseEntity<List<UsuarioBusquedaDto>> {
        val lista = usuarioRepository.findByNombreContains(nombre)
            .filter { it.perfilPublico }
            .map {
            UsuarioBusquedaDto(
                idFirebase = it.idFirebase,
                nombre = it.nombre,
                sexo = it.sexo,
                esPremium = it.esPremium,
                avatar = it.avatar
            )
        }
        return ResponseEntity.ok(lista)
    }

    private fun eliminarDeFirestore(uidUsuarioABorrar: String) {
        val autorizacion = FirebaseAuth.getInstance()
        autorizacion.revokeRefreshTokens(uidUsuarioABorrar)
        autorizacion.deleteUser(uidUsuarioABorrar)
        db.collection("Usuarios").document(uidUsuarioABorrar).delete()
    }

    private fun eliminarDeMongoDb(correo: String) {
        val usuario = usuarioRepository.findByCorreo(correo)
        if (usuario != null) {
            usuarioRepository.delete(usuario)
        } else {
            throw NotFoundException("Usuario con correo $correo no encontrado.")
        }
    }


    private fun validarUsuarioRegistro(usuario: UsuarioRegisterDTO): String? {

        if (usuario.nombre.isBlank()) {
            return "El nombre no puede estar vacío"
        }

        if (!usuario.correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex())) {
            return "El correo no es valido"
        }

        if (usuario.contrasena.isBlank() ||
            usuario.contrasena.length < 6 ||
            !usuario.contrasena.any { it.isDigit() }
        ) {
            return "La contraseña debe tener al menos 6 caracteres y contener al menos un número"
        }

        if (usuario.edad < 0) {
            return "La edad no puede ser negativa"
        }

        if (usuario.sexo != "H" && usuario.sexo != "M") {
            return "El sexo debe ser 'H' (hombre) o 'M' (mujer')"
        }

        if (usuarioRepository.findByCorreo(usuario.correo) != null) {
            return "El correo ya está registrado"
        }
        return null
    }




}