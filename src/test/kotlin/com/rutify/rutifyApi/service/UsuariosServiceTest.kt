package com.rutify.rutifyApi.service


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.UsuarioCredencialesDto
import com.rutify.rutifyApi.dto.UsuarioLoginDto
import com.rutify.rutifyApi.dto.UsuarioRegistroDTO
import com.rutify.rutifyApi.exception.exceptions.FirebaseUnavailableException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEstadisticasRepository
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.AuthUtils
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

class UsuariosServiceTest {

    // Mocks de dependencias
    private val usuarioRepository = mockk<IUsuarioRepository>(relaxed = true)
    private val estadisticasRepository = mockk<IEstadisticasRepository>()
    private val emailService = mockk<EmailService>(relaxed = true)
    private val rutinaRepository = mockk<IRutinasRepository>()
    private val firebaseAuthMock = mockk<FirebaseAuth>(relaxed = true)
    private val httpClientMock = mockk<HttpClient>(relaxed = true)
    private val apiKey = "fake-api-key"

    // Clase de prueba que sobrescribe los métodos protegidos
    private val usuariosService = object : UsuariosService(
        usuarioRepository,
        estadisticasRepository,
        emailService,
        rutinaRepository,
        apiKey,
        firebaseAuthMock
    ) {
        public override fun getFirebaseAuthInstance(): FirebaseAuth = firebaseAuthMock
        override fun createHttpClient(): HttpClient = httpClientMock
    }

    private val usuarioMock = Usuario(
        idFirebase = "abc123",
        nombre = "Test User",
        fechaNacimiento = LocalDate.now().minusYears(25),
        sexo = "H",
        correo = "test@mail.com",
        gimnasioId = null,
        esPremium = false,
        rol = "user",
        fechaUltimoReto = LocalDate.now()
    )
    private val usuarioRegistro = UsuarioRegistroDTO(
        nombre = "Juan Pérez",
        correo = "juan@example.com",
        contrasena = "pass123",
        fechaNacimiento = LocalDate.now().minusYears(20),
        sexo = "H"
    )
    private val estadisticas = Estadisticas(null, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0)

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(AuthUtils::class)
    }

    @Test
    fun loginUsuarios() {
        // Datos de entrada
        val loginDto = UsuarioCredencialesDto(correo = "test@mail.com", contrasena = "123456")

        // JSON simulado que devuelve Firebase
        val firebaseJsonResponse = """
            {
                "localId": "abc123",
                "idToken": "token123"
            }
        """.trimIndent()

        // Mock del HttpResponse<String>
        val httpResponseMock = mockk<HttpResponse<String>>()
        every { httpResponseMock.statusCode() } returns 200
        every { httpResponseMock.body() } returns firebaseJsonResponse

        // Mock del HttpClient para devolver el HttpResponse simulado
        every { httpClientMock.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns httpResponseMock

        // Mock del usuario que retorna el repositorio al buscar por localId

        every { usuarioRepository.findByIdFirebase("abc123") } returns usuarioMock

        // Llamamos al método a testear
        val response: ResponseEntity<UsuarioLoginDto> = usuariosService.loginUsuarios(loginDto)

        // Comprobamos el resultado esperado
        assertEquals(200, response.statusCode.value())
        assertEquals("Test User", response.body?.nombre)
        assertEquals("token123", response.body?.token)
    }

    @Test
    fun registrarUsuario() {
        // Cuando se busca el correo, no existe
        every { usuarioRepository.findByCorreo("juan@example.com") } returns null

        // Mockear FirebaseAuth y la creación de usuario
        val userRecordMock = mockk<UserRecord>()
        every { userRecordMock.uid } returns "uid123"
        every { usuariosService.getFirebaseAuthInstance().createUser(any()) } returns userRecordMock

        // Mockear el guardado de usuario para que devuelva el mismo objeto
        every { usuarioRepository.save(any()) } answers { firstArg() }

        val response = usuariosService.registrarUsuario(usuarioRegistro)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Juan Pérez", response.body?.nombre)
        assertEquals("juan@example.com", response.body?.correo)

        verify { usuarioRepository.save(match { it.idFirebase == "uid123" && it.nombre == "Juan Pérez" }) }
    }
    @Test
    fun registrarUsuario_correoInvalido_lanzaValidationException() {
        val usuarioRegistro = UsuarioRegistroDTO(
            nombre = "Juan Pérez",
            correo = "juanexample.com",
            contrasena = "pass123",
            fechaNacimiento = LocalDate.now().minusYears(20),
            sexo = "H"
        )

        val ex = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistro)
        }

        assertEquals("Error en la validacion (400). El correo no es valido", ex.message)
    }

    @Test
    fun registrarUsuario_fallaFirebase_lanzaFirebaseUnavailableException() {

        val usuariosServiceSpy = spyk(usuariosService)
        every { usuariosServiceSpy.validarUsuarioRegistro(usuarioRegistro) } returns null
        val firebaseAuthExceptionMock = mockk<FirebaseAuthException>(relaxed = true)
        every { firebaseAuthExceptionMock.message } returns "Firebase fuera de servicio"
        every { usuariosServiceSpy.getFirebaseAuthInstance() } returns firebaseAuthMock

        // Mockear la llamada a FirebaseAuth para que lance la excepción
        every { firebaseAuthMock.createUser(any()) } throws firebaseAuthExceptionMock

        val ex = assertThrows<FirebaseUnavailableException> {
            usuariosServiceSpy.registrarUsuario(usuarioRegistro)
        }

        assertEquals("Bad Gateway (502). Error con Firebase: Firebase fuera de servicio", ex.message)
    }

    @Test
    fun comprobarTodasCasuisticas_validarUsuarioRegistro() {
        // Caso 1: nombre vacío
        val usuario1 = UsuarioRegistroDTO(
            nombre = " ",
            correo = "correo@valido.com",
            contrasena = "pass1234",
            fechaNacimiento = LocalDate.now().minusYears(20),
            sexo = "H"
        )
        every { usuarioRepository.findByCorreo(any()) } returns null
        assertEquals("El nombre no puede estar vacío", usuariosService.validarUsuarioRegistro(usuario1))

        // Caso 2: correo no válido
        val usuario2 = usuario1.copy(nombre = "Nombre Valido", correo = "correo_invalido")
        assertEquals("El correo no es valido", usuariosService.validarUsuarioRegistro(usuario2))

        // Caso 3: contraseña vacía o menor que 6 caracteres
        val usuario3a = usuario2.copy(correo = "correo@valido.com", contrasena = "")
        assertEquals("La contraseña debe tener al menos 6 caracteres y contener al menos un número", usuariosService.validarUsuarioRegistro(usuario3a))
        val usuario3b = usuario3a.copy(contrasena = "abc") // menos de 6
        assertEquals("La contraseña debe tener al menos 6 caracteres y contener al menos un número", usuariosService.validarUsuarioRegistro(usuario3b))

        // Caso 4: edad menor a 16 años
        val usuario4 = usuario3a.copy(contrasena = "pass1234", fechaNacimiento = LocalDate.now().minusYears(15))
        assertEquals("La edad no puede ser menor a 16 años", usuariosService.validarUsuarioRegistro(usuario4))

        // Caso 5: sexo inválido
        val usuario5 = usuario4.copy(fechaNacimiento = LocalDate.now().minusYears(20), sexo = "X")
        assertEquals("El sexo debe ser 'H' (hombre), 'M' (mujer) O 'O' (otro sexo)", usuariosService.validarUsuarioRegistro(usuario5))

        // Caso 6: correo ya registrado
        val usuario6 = usuario5.copy(sexo = "H", correo = "correo@existente.com")
        every { usuarioRepository.findByCorreo("correo@existente.com") } returns usuarioMock // simula que existe
        assertEquals("El correo ya está registrado", usuariosService.validarUsuarioRegistro(usuario6))

        // Caso 7: usuario válido
        val usuarioValido = usuario6.copy(correo = "nuevo@valido.com", sexo = "O")
        every { usuarioRepository.findByCorreo("nuevo@valido.com") } returns null
        val resultado = usuariosService.validarUsuarioRegistro(usuarioValido)
        assertNull(resultado)
    }

    @Test
    fun loginUsuarios_errorCredenciales() {
        val loginDto = UsuarioCredencialesDto(correo = "correo@invalido.com", contrasena = "pass1234")

        val firebaseErrorResponse = """
        {
            "error": {
                "message": "EMAIL_NOT_FOUND"
            }
        }
    """.trimIndent()

        val httpResponseMock = mockk<HttpResponse<String>>()
        every { httpResponseMock.statusCode() } returns 400
        every { httpResponseMock.body() } returns firebaseErrorResponse

        every { httpClientMock.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns httpResponseMock

        val exception = assertThrows<UnauthorizedException> {
            usuariosService.loginUsuarios(loginDto)
        }

        assert(exception.message!!.contains("Credenciales incorrectas"))
        assert(exception.message!!.contains("EMAIL_NOT_FOUND"))
    }


    @Test
    fun eliminarUsuarioPorCorreo_exito() {
        val correo = "test@mail.com"
        val uidActual = "uid-actual"
        val authentication = mockk<Authentication>()
        every { authentication.name } returns uidActual

        // Mock del repositorio
        every { usuarioRepository.findByCorreo(correo) } returns usuarioMock

        mockkObject(AuthUtils)

        // Mock de métodos eliminarDeFirestore y eliminarDeMongoDb
        // Si son métodos internos, es mejor que estén en otra clase para poder mockear.
        // Aquí asumo que son internos y creamos spies para mockear
        val usuariosServiceSpy = spyk(usuariosService)

        every { AuthUtils.verificarPermisos(usuarioMock, any(), any()) } just Runs
        every { usuariosServiceSpy.eliminarDeFirestore(usuarioMock.idFirebase) } just Runs
        every { usuariosServiceSpy.eliminarDeMongoDb(correo) } just Runs

        // Mock del emailService
        every { emailService.enviarCorreoNotificacion(match { true }, any(), any()) } just Runs

        // Ejecutar el método (usando el spy para que se usen los mocks de eliminar...)
        val response = usuariosServiceSpy.eliminarUsuarioPorCorreo(correo, authentication)

        // Comprobar que devuelve noContent (204)
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        // Verificar que se llamó a cada método mockeado
        verify { usuarioRepository.findByCorreo(correo) }
        verify { AuthUtils.verificarPermisos(usuarioMock, uidActual) }
        verify { usuariosServiceSpy.eliminarDeFirestore(usuarioMock.idFirebase) }
        verify { usuariosServiceSpy.eliminarDeMongoDb(correo) }
        verify { emailService.enviarCorreoNotificacion(correo, any(), any()) }
    }
    @Test
    fun eliminarDeFirestore_exito() {
        val uid = "uid-usuario"

        // Crear spy del service y mockear getFirebaseAuthInstance
        val usuariosServiceSpy = spyk(usuariosService)
        every { usuariosServiceSpy.getFirebaseAuthInstance() } returns firebaseAuthMock

        // Ejecutar el método
        usuariosServiceSpy.eliminarDeFirestore(uid)

        // Verificar llamadas
        verify { firebaseAuthMock.revokeRefreshTokens(uid) }
        verify { firebaseAuthMock.deleteUser(uid) }
    }

    @Test
    fun eliminarDeMongoDb_exito() {
        val correo = "test@mail.com"

        every { usuarioRepository.findByCorreo(correo) } returns usuarioMock
        every { usuarioRepository.delete(usuarioMock) } just Runs

        usuariosService.eliminarDeMongoDb(correo)

        verify { usuarioRepository.findByCorreo(correo) }
        verify { usuarioRepository.delete(usuarioMock) }
    }

    @Test
    fun eliminarDeMongoDb_usuarioNoEncontrado_lanzaExcepcion() {
        val correo = "inexistente@mail.com"

        every { usuarioRepository.findByCorreo(correo) } returns null

        val exception = assertThrows<NotFoundException> {
            usuariosService.eliminarDeMongoDb(correo)
        }

        assertEquals("Not found exception (404). Usuario con correo inexistente@mail.com no encontrado.", exception.message)
        verify { usuarioRepository.findByCorreo(correo) }
    }

    @Test
    fun obtenerDetalleUsuario_perfilPublico_devuelveInformacion() {
        val idFirebase = "123"
        val authentication = mockk<Authentication>()
        every { authentication.name } returns "otroUid"

        val usuario = usuarioMock.copy(idFirebase = idFirebase, perfilPublico = true)
        val totalRutinas = 2L

        every { usuarioRepository.findByIdFirebase(idFirebase) } returns usuario
        every { estadisticasRepository.findByIdFirebase(idFirebase) } returns estadisticas
        every { rutinaRepository.countByCreadorId(idFirebase) } returns totalRutinas

        val response = usuariosService.obtenerDetalleUsuario(idFirebase, authentication)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(usuario.nombre, response.body?.nombre)
        assertEquals(usuario.correo, response.body?.correo)
        assertEquals(totalRutinas, response.body?.countRutinas)
    }

    @Test
    fun obtenerDetalleUsuario_perfilPrivadoPeroMismoUsuario_devuelveInformacion() {
        val idFirebase = "123"
        val authentication = mockk<Authentication>()
        every { authentication.name } returns idFirebase

        val usuario = usuarioMock.copy(idFirebase = idFirebase, perfilPublico = false)
        val estadisticas = Estadisticas(null, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0)
        val totalRutinas = 3L

        every { usuarioRepository.findByIdFirebase(idFirebase) } returns usuario
        every { estadisticasRepository.findByIdFirebase(idFirebase) } returns estadisticas
        every { rutinaRepository.countByCreadorId(idFirebase) } returns totalRutinas

        val response = usuariosService.obtenerDetalleUsuario(idFirebase, authentication)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(usuario.nombre, response.body?.nombre)
        assertEquals(usuario.correo, response.body?.correo)
    }

    @Test
    fun obtenerDetalleUsuario_perfilPrivadoOtroUsuario_lanzaUnauthorized() {
        val idFirebase = "123"
        val otroUid = "otro-uid"
        val authentication = mockk<Authentication>()
        every { authentication.name } returns otroUid

        val usuario = usuarioMock.copy(idFirebase = idFirebase, perfilPublico = false)

        every { usuarioRepository.findByIdFirebase(idFirebase) } returns usuario

        // Mock innecesarios pero obligatorios por cómo Kotlin evalúa
        every { estadisticasRepository.findByIdFirebase(any()) } returns estadisticas
        every { rutinaRepository.countByCreadorId(any()) } returns 0

        val exception = assertThrows<UnauthorizedException> {
            usuariosService.obtenerDetalleUsuario(idFirebase, authentication)
        }

        assertEquals("Unauthorized (401). Este usuario tiene el perfil en privado", exception.message)
    }
    @Test
    fun buscarUsuariosPorNombre_conResultados_devuelveListaYHasNextTrue() {
        val nombreBuscado = "Juan"
        val pagina = 0
        val tamano = 2

        val usuarios = listOf(
            usuarioMock.copy(nombre = "Juan Pérez"),
            usuarioMock.copy(nombre = "Juanita Gómez")
        )

        val page = mockk<Page<Usuario>>()
        every { page.content } returns usuarios
        every { page.hasNext() } returns true

        every {
            usuarioRepository.findByNombreContainsAndPerfilPublicoTrue(nombreBuscado, PageRequest.of(pagina, tamano))
        } returns page

        val response = usuariosService.buscarUsuariosPorNombre(nombreBuscado, pagina, tamano)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.usuarios?.size)
        assertTrue(response.body?.hasNext == true)
    }
    @Test
    fun buscarUsuariosPorNombre_sinResultados_devuelveListaVacia() {
        val nombreBuscado = "NoExiste"
        val pagina = 0
        val tamano = 10

        val page = mockk<Page<Usuario>>()
        every { page.content } returns emptyList()
        every { page.hasNext() } returns false

        every {
            usuarioRepository.findByNombreContainsAndPerfilPublicoTrue(nombreBuscado, PageRequest.of(pagina, tamano))
        } returns page

        val response = usuariosService.buscarUsuariosPorNombre(nombreBuscado, pagina, tamano)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.usuarios?.size)
        assertFalse(response.body?.hasNext == true)
    }
    @Test
    fun buscarUsuariosPorNombre_ultimaPagina_hasNextFalse() {
        val nombreBuscado = "Ana"
        val pagina = 2
        val tamano = 5

        val usuarios = listOf(
            usuarioMock.copy(nombre = "Ana Torres")
        )

        val page = mockk<Page<Usuario>>()
        every { page.content } returns usuarios
        every { page.hasNext() } returns false

        every {
            usuarioRepository.findByNombreContainsAndPerfilPublicoTrue(nombreBuscado, PageRequest.of(pagina, tamano))
        } returns page

        val response = usuariosService.buscarUsuariosPorNombre(nombreBuscado, pagina, tamano)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body?.usuarios?.size)
        assertFalse(response.body?.hasNext == true)
    }


    @Test
    fun getApiKey() {
    }

    @Test
    fun getFirebaseAuth() {
    }

    @Test
    fun getHttpClient() {
    }
}