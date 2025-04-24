package com.rutify.rutifyApi.controller

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.UsuarioCredencialesDto
import com.rutify.rutifyApi.dto.UsuarioLoginDto
import com.rutify.rutifyApi.dto.UsuarioRegisterDTO
import com.rutify.rutifyApi.dto.UsuarioregistradoDto
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.service.UsuariosService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class UsuariosControllerTest {

    @InjectMocks
    private lateinit var usuariosService:UsuariosService

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var userRecord: UserRecord

    @Mock
    private lateinit var db: Firestore

    @Mock
    private lateinit var usuarioRepository: IUsuarioRepository

    @Mock
    private lateinit var mockHttpClient: HttpClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val collectionReference = mock(CollectionReference::class.java)
        val documentReference = mock(DocumentReference::class.java)
        val apiFuture = mock<ApiFuture<WriteResult>>()


        usuariosService.httpClient = mockHttpClient
        usuariosService.apiKey = "mockedApiKey"

        // Mock FirebaseAuth behavior
        whenever(firebaseAuth.createUser(any())).thenReturn(userRecord)
        // Mock UserRecord behavior
        whenever(userRecord.uid).thenReturn("mockedUid")
        whenever(db.collection("Usuarios")).thenReturn(collectionReference)
        whenever(collectionReference.document(any())).thenReturn(documentReference)
        whenever(documentReference.set(any())).thenReturn(apiFuture)
        usuariosService.db = db

    }

    @Test
    fun registrarUsuario_Ok() {
        val usuarioRegistroDTO = UsuarioRegisterDTO(
            nombre = "Carlos Gómez",
            correo = "carlos.gomez@example.com",
            contrasena = "123456",
            edad = 25,
            sexo = "M"
        )

        val usuarioRegistradoDto = UsuarioregistradoDto(
            nombre = "Carlos Gómez",
            correo = "carlos.gomez@example.com"
        )

        val usuarioMock = Usuario(
            idFirebase = "mockedUid",
            nombre = "Carlos Gómez",
            edad = 25,
            sexo = "M",
            correo = "carlos.gomez@example.com",
            gimnasioId = null,
            esPremium = false
        )

        // Configurar el mock del repositorio para que devuelva el objeto correcto
        whenever(usuarioRepository.save(usuarioMock)).thenReturn(usuarioMock)
        // Act
        val response = usuariosService.registrarUsuario(usuarioRegistroDTO)

        // Assert
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.nombre == usuarioRegistradoDto.nombre)
        assert(response.body?.correo == usuarioRegistradoDto.correo)
    }

    @Test
    fun registrarUsuario_Error() {
        val usuarioRegistroDTO = UsuarioRegisterDTO(
            nombre = "",
            correo = "",
            contrasena = "",
            edad = -3,
            sexo = "hombre"
        )
        val usuarioExistente = Usuario(
            idFirebase = "mockedUid",
            nombre = "Carlos Gómez",
            edad = 25,
            sexo = "H",
            correo = "correo@ejemplo.com",
            gimnasioId = null,
            esPremium = false
        )
        // Act
        val exceptionNombre = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistroDTO)
        }
        usuarioRegistroDTO.nombre = "lorem ipsum"
        val exceptionCorreo = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistroDTO)
        }
        usuarioRegistroDTO.correo = "correo@ejemplo.com"
        val exceptionContrasena = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistroDTO)
        }
        usuarioRegistroDTO.contrasena = "123456"
        val exceptionEdad = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistroDTO)
        }
        usuarioRegistroDTO.edad = 25
        val exceptionSexo = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistroDTO)
        }
        usuarioRegistroDTO.sexo = "H"
        whenever(usuarioRepository.findByCorreo(usuarioRegistroDTO.correo)).thenReturn(usuarioExistente)
        val exceptionCorreoExistente = assertThrows<ValidationException> {
            usuariosService.registrarUsuario(usuarioRegistroDTO)
        }

        //Assert
        assert(exceptionNombre.message == "Error en la validacion (400). El nombre no puede estar vacío")
        assert(exceptionCorreo.message == "Error en la validacion (400). El correo no es valido")
        assert(exceptionContrasena.message == "Error en la validacion (400). La contraseña debe tener al menos 6 caracteres y contener al menos un número")
        assert(exceptionEdad.message == "Error en la validacion (400). La edad no puede ser negativa")
        assert(exceptionSexo.message == "Error en la validacion (400). El sexo debe ser 'H' (hombre) o 'M' (mujer')")
        assert(exceptionCorreoExistente.message == "Error en la validacion (400). El correo ya está registrado")
    }

    @Test
    fun loginUsuarios_ok() {
        // Preparamos el DTO de login
        val loginDto = UsuarioCredencialesDto(
            correo = "test@example.com",
            contrasena = "password123"
        )

        // Mock de la respuesta HTTP de Firebase
        val mockFirebaseResponse = """
        {
            "kind": "identitytoolkit#VerifyPasswordResponse",
            "localId": "mockedUid",
            "email": "test@example.com",
            "displayName": "",
            "idToken": "mockToken123",
            "registered": true,
            "refreshToken": "mockRefreshToken",
            "expiresIn": "3600"
        }
    """.trimIndent()

        // Mock de HttpClient

        val mockHttpResponse= mock(HttpResponse::class.java) as HttpResponse<String>
        val mockCollectionReference = mock(CollectionReference::class.java)
        val mockQuery = mock(Query::class.java)
        val mockApiFutureQuerySnapshot = mock(ApiFuture::class.java) as ApiFuture<QuerySnapshot>
        val mockQueryDocumentSnapshot = mock(QueryDocumentSnapshot::class.java)
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)

        whenever(mockHttpResponse.statusCode()).thenReturn(200)
        whenever(mockHttpResponse.body()).thenReturn(mockFirebaseResponse)

        whenever(mockHttpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()))
            .thenReturn(mockHttpResponse)

        // Mock de Firestore
        whenever(mockQueryDocumentSnapshot.getString("nombre")).thenReturn("Test User")

        whenever(mockQuerySnapshot.isEmpty).thenReturn(false)
        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockQueryDocumentSnapshot))

        whenever(db.collection("Usuarios")).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.whereEqualTo("email", loginDto.correo)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(mockApiFutureQuerySnapshot)
        whenever(mockApiFutureQuerySnapshot.get()).thenReturn(mockQuerySnapshot)
        mockStatic(HttpClient::class.java)
        whenever(HttpClient.newHttpClient()).thenReturn(mockHttpClient)

        // Inyectar el mock HttpClient (necesitarías modificar el servicio para permitir inyección)

        // Ejecutar
        val response = usuariosService.loginUsuarios(loginDto)

        // Verificar
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Test User", response.body?.nombre)
        assertEquals("mockToken123", response.body?.token)
    }

    @Test
    fun eliminarCuenta() {
    }

    @Test
    fun buscarUsuariosPorNombre() {
    }

    @Test
    fun obtenerDetalleUsuario() {
    }

    @Test
    fun actualizarCorreo() {
    }
}