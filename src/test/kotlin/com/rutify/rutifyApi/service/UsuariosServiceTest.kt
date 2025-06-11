package com.rutify.rutifyApi.service


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.ActualizarUsuarioDTO
import com.rutify.rutifyApi.dto.UsuarioCredencialesDto
import com.rutify.rutifyApi.dto.UsuarioLoginDto
import com.rutify.rutifyApi.dto.UsuarioRegistroDTO
import com.rutify.rutifyApi.exception.exceptions.FirebaseUnavailableException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.*
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
    private val estadisticasService = mockk<EstadisticasService>()
    private val emailService = mockk<EmailService>(relaxed = true)
    private val rutinaRepository = mockk<IRutinasRepository>()
    private val firebaseAuthMock = mockk<FirebaseAuth>(relaxed = true)
    private val httpClientMock = mockk<HttpClient>(relaxed = true)
    private val mensajeService = mockk<MensajesService>(relaxed = true)
    private val comentarioService = mockk<ComentarioService>(relaxed = true)
    private val votosService = mockk<VotosService>(relaxed = true)
    private val estadisticasDiariasService = mockk<EstadisticasDiariasService>(relaxed = true)
    private val compraRepository = mockk<CompraRepository>(relaxed = true)
    private val reporteService = mockk<ReporteService>(relaxed = true)
    private val rutinaService = mockk<RutinaService>(relaxed = true)
    private val apiKey = "fake-api-key"
    private val comestico = Cosmetico(tipo = "camiseta", imagenUrl = "url-camiseta", nombre = "", precioMonedas = 1)
    private val authentication = mockk<Authentication>()
    private val uidActual = "uid-actual"
    // Clase de prueba que sobrescribe los métodos protegidos
    private val usuariosService = object : UsuariosService(
        usuarioRepository,
        estadisticasService,
        emailService,
        rutinaRepository,
        comentarioService,
        votosService,
        estadisticasDiariasService,
        compraRepository,
        reporteService,
        rutinaService,
        mensajeService,
        apiKey,
        firebaseAuthMock
    ) {
        override fun getFirebaseAuthInstance(): FirebaseAuth = firebaseAuthMock
        override fun createHttpClient(): HttpClient = httpClientMock
    }

    val usuariosServiceSpy = spyk(usuariosService)

    private val usuarioMock = Usuario(
        idFirebase = "abc123",
        nombre = "Test User",
        fechaNacimiento = LocalDate.now().minusYears(25),
        sexo = "H",
        correo = "test@mail.com",
        gimnasioId = null,
        perfilPublico = false,
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
    private val dto = ActualizarUsuarioDTO(
        correo = usuarioMock.correo,
        nombre = "NuevoNombre",
        sexo = "M",
        fechaNacimiento = LocalDate.of(2001, 1, 1),
        perfilPublico = false,
        avatar = "nuevo-avatar.png"
    )
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

        every { usuarioRepository.findByCorreo(any()) } returns null
        assertDoesNotThrow { usuariosServiceSpy.validarUsuarioRegistro(usuarioRegistro) }
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

        var exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario1)
        }
        assertEquals("Error en la validacion (400). El nombre no puede estar vacío", exception.message)

        // Caso 2: correo no válido
        val usuario2 = usuario1.copy(nombre = "Nombre Valido", correo = "correo_invalido")
        exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario2)
        }
        assertEquals("Error en la validacion (400). El correo no es valido", exception.message)

        // Caso 3: contraseña vacía o menor que 6 caracteres
        val usuario3a = usuario2.copy(correo = "correo@valido.com", contrasena = "")
        exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario3a)
        }
        assertEquals("Error en la validacion (400). La contraseña debe tener al menos 6 caracteres y contener al menos un número", exception.message)
        val usuario3b = usuario3a.copy(contrasena = "abc") // menos de 6
        exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario3b)
        }
        assertEquals("Error en la validacion (400). La contraseña debe tener al menos 6 caracteres y contener al menos un número", exception.message)

        // Caso 4: edad menor a 16 años
        val usuario4 = usuario3a.copy(contrasena = "pass1234", fechaNacimiento = LocalDate.now().minusYears(15))
        exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario4)
        }
        assertEquals("Error en la validacion (400). La edad no puede ser menor a 16 años", exception.message)

        // Caso 5: sexo inválido
        val usuario5 = usuario4.copy(fechaNacimiento = LocalDate.now().minusYears(20), sexo = "X")
        exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario5)
        }
        assertEquals("Error en la validacion (400). El sexo debe ser 'H' (hombre), 'M' (mujer) O 'O' (otro sexo)", exception.message)

        // Caso 6: correo ya registrado
        val usuario6 = usuario5.copy(sexo = "H", correo = "correo@existente.com")
        every { usuarioRepository.findByCorreo("correo@existente.com") } returns usuarioMock // simula que existe
        exception = assertThrows<ValidationException> {
            usuariosService.validarUsuarioRegistro(usuario6)
        }
        assertEquals("Error en la validacion (400). El correo ya está registrado", exception.message)

        // Caso 7: usuario válido
        val usuarioValido = usuario6.copy(correo = "nuevo@valido.com", sexo = "O")

        every { usuarioRepository.findByCorreo("nuevo@valido.com") } returns null
        assertDoesNotThrow {
            usuariosService.validarUsuarioRegistro(usuarioValido)
        }
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
        every { votosService.eliminarVotosDeUnsuario(any(),any()) } just Runs
        every { comentarioService.eliminarComentariosDeUnUsuario(any(),any()) } just Runs
        every { compraRepository.deleteByIdUsuario(any()) } returns 1L
        every { estadisticasService.deleteByIdUsuario(any(),any()) } just Runs
        every { estadisticasDiariasService.eliminarEstadisticas(any(),any()) } just Runs
        every { reporteService.eliminarReportes(any(),any()) } just Runs
        every { rutinaService.eliminarTodasRutinasDelusuario(any(),any()) } just Runs
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
    fun eliminarUsuarioPorCorreoAdmin_exito() {
        val correo = "test@mail.com"

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
        every { votosService.eliminarVotosDeUnsuario(any(),any()) } just Runs
        every { comentarioService.eliminarComentariosDeUnUsuario(any(),any()) } just Runs
        every { compraRepository.deleteByIdUsuario(any()) } returns 1L
        every { estadisticasService.deleteByIdUsuario(any(),any()) } just Runs
        every { estadisticasDiariasService.eliminarEstadisticas(any(),any()) } just Runs
        every { reporteService.eliminarReportes(any(),any()) } just Runs
        every { rutinaService.eliminarTodasRutinasDelusuario(any(),any()) } just Runs
        // Mock del emailService
        every { emailService.enviarCorreoNotificacion(match { true }, any(), any()) } just Runs

        // Ejecutar el método (usando el spy para que se usen los mocks de eliminar...)
        val response = usuariosServiceSpy.eliminarUsuarioPorCorreo(correo, authentication,false)

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
        every { estadisticasService.findByIdFirebase(idFirebase) } returns estadisticas
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
        every { estadisticasService.findByIdFirebase(idFirebase) } returns estadisticas
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
        every { estadisticasService.findByIdFirebase(any()) } returns estadisticas
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
        assertEquals(2, response.body?.size)
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
        assertEquals(0, response.body?.size)
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
        assertEquals(1, response.body?.size)
    }

    @Test
    fun eliminarUsuarioPorCorreo_correoNoExiste() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByCorreo(any()) } returns null
        val exception = assertThrows<NotFoundException> {
            usuariosServiceSpy.eliminarUsuarioPorCorreo("cprueba1@gmail.com", authentication)
        }
        assertEquals("Not found exception (404). Usuario con correo cprueba1@gmail.com no encontrado.", exception.message)
    }

    @Test
    fun obtenerDetalleUsuario_exito() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns usuarioMock.copy(perfilPublico = true)
        every { estadisticasService.findByIdFirebase(any()) } returns estadisticas
        every { rutinaRepository.countByCreadorId(any()) } returns 1
        every { comentarioService.countByIdFirebaseAndIdComentarioPadreIsNull(any()) } returns 1
        every { votosService.countByIdFirebase(any()) } returns 1
        val response = usuariosServiceSpy.obtenerDetalleUsuario("uid-sdsdsdactual", authentication)

        assertEquals(200, response.statusCode.value())
    }

    @Test
    fun obtenerDetalleUsuario_unaturized() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns usuarioMock
        val exception = assertThrows<UnauthorizedException> {
            usuariosServiceSpy.obtenerDetalleUsuario("uid-sdsdsdactual", authentication)
        }
        assertEquals("Unauthorized (401). Este usuario tiene el perfil en privado", exception.message)
    }

    @Test
    fun obtenerDetalleUsuario_correoNoExiste() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns null
        val exception = assertThrows<NotFoundException> {
            usuariosServiceSpy.obtenerDetalleUsuario("uid-actual", authentication)
        }
        assertEquals("Not found exception (404). ", exception.message)
    }

    @Test
    fun anadirMonedas_exito() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns usuarioMock
        every { usuarioRepository.save(any()) } answers { firstArg() }
        assertDoesNotThrow {
            usuariosServiceSpy.anadirMonedas("uid-actual", 12)
        }
    }

    @Test
    fun anadirMonedas_correoNoExiste() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns null
        val exception = assertThrows<NotFoundException> {
            usuariosServiceSpy.anadirMonedas("uid-actual", 12)
        }
        assertEquals("Not found exception (404). ", exception.message)
    }

    @Test
    fun actualizarCuenta_exito() {


        val adminMock = usuarioMock.copy(rol = "admin")

        every { authentication.name } returns adminMock.idFirebase
        every { usuarioRepository.findByIdFirebase(adminMock.idFirebase) } returns adminMock
        every { usuarioRepository.findByCorreo(dto.correo) } returns usuarioMock
        every { usuarioRepository.save(any()) } returns usuarioMock
        every { mensajeService.obtenerMensaje(any()) } returns "Usuario no encontrado"

        val response = usuariosService.actualizarCuenta(authentication, dto)

        assertEquals(200, response.statusCode.value())
        assertEquals("NuevoNombre", usuarioMock.nombre)
    }

    @Test
    fun actualizarCuenta_usuarioSolicitanteNoExiste() {

        every { authentication.name } returns "uid-desconocido"
        every { usuarioRepository.findByIdFirebase("uid-desconocido") } returns null

        val exception = assertThrows<NotFoundException> {
            usuariosService.actualizarCuenta(authentication, dto)
        }

        assertEquals("Not found exception (404). Usuario solicitante no encontrado", exception.message)
    }

    @Test
    fun actualizarCuenta_usuarioACambiarNoExiste() {
        val adminMock = usuarioMock.copy(rol = "admin")

        every { authentication.name } returns adminMock.idFirebase
        every { usuarioRepository.findByIdFirebase(adminMock.idFirebase) } returns adminMock
        every { usuarioRepository.findByCorreo(dto.correo) } returns null
        every { mensajeService.obtenerMensaje(any()) } returns "Usuario no encontrado"

        val exception = assertThrows<NotFoundException> {
            usuariosService.actualizarCuenta(authentication, dto)
        }

        assertEquals("Not found exception (404). Usuario no encontrado", exception.message)
    }

    @Test
    fun actualizarCuenta_usuarioNoEsAdmin() {
        val usuarioNoAdmin = usuarioMock.copy(rol = "usuario")

        every { authentication.name } returns usuarioNoAdmin.idFirebase
        every { usuarioRepository.findByIdFirebase(usuarioNoAdmin.idFirebase) } returns usuarioNoAdmin
        every { usuarioRepository.findByCorreo(dto.correo) } returns usuarioMock

        val exception = assertThrows<ClassCastException> {
            usuariosService.actualizarCuenta(authentication, dto)
        }

    }

    @Test
    fun actualizarCuenta_correosNoCoinciden() {
        val adminMock = usuarioMock.copy(rol = "admin")
        val usuarioACambiar = usuarioMock.copy(correo = "distinto@correo.com")

        every { authentication.name } returns adminMock.idFirebase
        every { usuarioRepository.findByIdFirebase(adminMock.idFirebase) } returns adminMock
        every { usuarioRepository.findByCorreo(dto.correo) } returns usuarioACambiar

        val exception = assertThrows<ClassCastException> {
            usuariosService.actualizarCuenta(authentication, dto)
        }

    }

    @Test
    fun validarActualizarUsuarioDTO_valido_completo() {

        assertDoesNotThrow {
            usuariosService.validarActualizarUsuarioDTO(dto.copy(
                nombre = "Juan",
                sexo = "H",
                fechaNacimiento = LocalDate.of(2000, 1, 1),
                correo = "correo@correo.com"
            ))
        }
    }

    @Test
    fun validarActualizarUsuarioDTO_valido_nombreYsexoNull() {

        assertDoesNotThrow {
            usuariosService.validarActualizarUsuarioDTO(dto.copy(
                nombre = null,
                sexo = null,
                fechaNacimiento = LocalDate.of(2000, 1, 1),
                correo = "correo@correo.com"
            ))
        }
    }

    @Test
    fun validarActualizarUsuarioDTO_nombreVacio() {
        val exception = assertThrows<ValidationException> {
            usuariosService.validarActualizarUsuarioDTO(dto.copy(
                nombre = "   ",
                correo = "correo@correo.com"
            ))
        }

        assertEquals("Error en la validacion (400). El nombre no puede estar vacío", exception.message)
    }

    @Test
    fun validarActualizarUsuarioDTO_fechaNacimientoMenorA16() {
        val fechaJoven = LocalDate.now().minusYears(15)

        val exception = assertThrows<ValidationException> {
            usuariosService.validarActualizarUsuarioDTO(dto.copy(
                fechaNacimiento = fechaJoven,
                correo = "correo@correo.com"
            ))
        }

        assertEquals("Error en la validacion (400). La edad no puede ser negativa", exception.message)
    }

    @Test
    fun validarActualizarUsuarioDTO_sexoInvalido() {

        val exception = assertThrows<ValidationException> {
            usuariosService.validarActualizarUsuarioDTO(dto.copy(
                sexo = "X",
                correo = "correo@correo.com"
            ))
        }

        assertEquals("Error en la validacion (400). El sexo debe ser 'O'(Otro) 'H' (hombre) o 'M' (mujer')", exception.message)
    }

    @Test
    fun esAdmin_usuarioEsAdmin() {
        val usuario = usuarioMock.copy(rol = "admin")
        every { usuarioRepository.findByIdFirebase("uid-admin") } returns usuario

        val response = usuariosService.EsAdmin("uid-admin")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!)
    }

    @Test
    fun esAdmin_usuarioNoEsAdmin() {
        val usuario = usuarioMock.copy(rol = "usuario")
        every { usuarioRepository.findByIdFirebase("uid-user") } returns usuario

        val response = usuariosService.EsAdmin("uid-user")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertFalse(response.body!!)
    }

    @Test
    fun esAdmin_usuarioNoEncontrado() {
        every { usuarioRepository.findByIdFirebase("uid-invalido") } returns null
        every { mensajeService.obtenerMensaje("UsuarioNoEncontrado") } returns "Usuario con ese ID no encontrado"

        val exception = assertThrows<NotFoundException> {
            usuariosService.EsAdmin("uid-invalido")
        }

        assertEquals("Not found exception (404). Usuario con ese ID no encontrado", exception.message)
    }

    @Test
    fun marcarRetoDiario_yaMarcadoHoy() {
        every { authentication.name } returns "uid-actual"
        val usuario = usuarioMock.copy(fechaUltimoReto = LocalDate.now())
        every { usuarioRepository.findByIdFirebase("uid-actual") } returns usuario

        val response = usuariosServiceSpy.marcarRetoDiario(authentication)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!)
    }

    @Test
    fun marcarRetoDiario_noMarcadoHoy() {
        every { authentication.name } returns "uid-actual"
        val usuario = usuarioMock.copy(fechaUltimoReto = LocalDate.now().minusDays(1))
        every { usuarioRepository.findByIdFirebase("uid-actual") } returns usuario
        every { usuarioRepository.save(any()) } answers { firstArg() }

        val response = usuariosServiceSpy.marcarRetoDiario(authentication)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertFalse(response.body!!)
    }

    @Test
    fun marcarRetoDiario_usuarioNoEncontrado() {
        every { authentication.name } returns "uid-invalido"
        every { usuarioRepository.findByIdFirebase("uid-invalido") } returns null
        every { mensajeService.obtenerMensaje("UsuarioNoEncontrado") } returns "Usuario no encontrado"

        val exception = assertThrows<NotFoundException> {
            usuariosServiceSpy.marcarRetoDiario(authentication)
        }

        assertEquals("Not found exception (404). Usuario no encontrado", exception.message)
    }

    @Test
    fun quitarMonedas_exito() {
        val usuario = usuarioMock.copy(monedas = 100)
        every { usuariosServiceSpy.obtenerUsuario("uid-actual") } returns usuario
        every { usuarioRepository.save(any()) } answers { firstArg() }

        assertDoesNotThrow {
            usuariosServiceSpy.quitarMonedas("uid-actual", 50)
        }

        assertEquals(50, usuario.monedas)
    }

    @Test
    fun quitarMonedas_monedasInsuficientes() {
        val usuario = usuarioMock.copy(monedas = 20)
        every { usuariosServiceSpy.obtenerUsuario("uid-actual") } returns usuario

        val exception = assertThrows<ValidationException> {
            usuariosServiceSpy.quitarMonedas("uid-actual", 50)
        }

        assertEquals("Error en la validacion (400). no hay monedas suficientes", exception.message)
    }

    @Test
    fun quitarMonedas_usuarioNoExiste() {
        every { usuariosServiceSpy.obtenerUsuario("uid-invalido") } throws NotFoundException("Usuario no encontrado")

        val exception = assertThrows<NotFoundException> {
            usuariosServiceSpy.quitarMonedas("uid-invalido", 10)
        }

        assertEquals("Not found exception (404). Usuario no encontrado", exception.message)
    }

    @Test
    fun aplicarCosmetico_piel_exito() {
        every { authentication.name } returns uidActual
        val usuario = usuarioMock.copy(indumentaria = usuarioMock.indumentaria.copy())
        every { usuarioRepository.findByIdFirebase(uidActual) } returns usuario
        every { usuarioRepository.save(any()) } answers { firstArg() }

        val response = usuariosServiceSpy.aplicarCosmetico(authentication, comestico)

        assertEquals("https://i.ibb.co/mkfD3hj/brazos-1.webp", usuario.indumentaria.colorPiel)
        assertEquals(200, response.statusCode.value())
    }

    @Test
    fun aplicarCosmetico_tipoDesconocido() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns usuarioMock

        val exception = assertThrows<ValidationException> {
            usuariosServiceSpy.aplicarCosmetico(authentication, comestico.copy(tipo = "sadsdsds"))
        }

        assertEquals("Error en la validacion (400). Tipo de cosmético desconocido: sadsdsds", exception.message)
    }

    @Test
    fun aplicarCosmetico_usuarioNoExiste() {
        every { authentication.name } returns uidActual
        every { usuarioRepository.findByIdFirebase(any()) } returns null

        val exception = assertThrows<NotFoundException> {
            usuariosServiceSpy.aplicarCosmetico(authentication, comestico)
        }

        assertEquals("Not found exception (404). Usuario no encontrado", exception.message)
    }

    @Test
    fun findByReportesGreaterThanOrderByReportesAsc_exito() {
        val usuariosReportados = listOf(
            usuarioMock.copy(reportes = 3),
            usuarioMock.copy(reportes = 5)
        )

        every { usuarioRepository.findByReportesGreaterThanOrderByReportesAsc() } returns usuariosReportados

        val resultado = usuariosServiceSpy.findByReportesGreaterThanOrderByReportesAsc()

        assertEquals(2, resultado.size)
        assertTrue(resultado[0].reportes <= resultado[1].reportes)
    }

}