package com.rutify.rutifyApi.service

import com.google.firebase.auth.FirebaseAuth
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.repository.IUsuarioRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpClient
import java.time.LocalDate

class ServiceBaseTest {

    private val usuarioRepository = mockk<IUsuarioRepository>(relaxed = true)

    // Usamos una instancia real para probar getters y setters de httpClient
    private val serviceBase = ServiceBase(usuarioRepository)

    @Test
    fun getHttpClient() {
        val httpClientMock = mockk<HttpClient>()
        serviceBase.httpClient = httpClientMock
        assertEquals(httpClientMock, serviceBase.httpClient)
    }

    @Test
    fun setHttpClient() {
        val httpClientMock = mockk<HttpClient>()
        serviceBase.httpClient = httpClientMock

        assertEquals(httpClientMock, serviceBase.httpClient)
    }

    @Test
    fun obtenerUsuario() {
        val idFirebase = "123"
        val usuarioMock = Usuario(
            idFirebase = idFirebase,
            nombre = "Juan",
            fechaNacimiento = LocalDate.now().minusYears(25),
            sexo = "H",
            correo = "juan@example.com",
            gimnasioId = null,
            esPremium = false,
            rol = "user",
            fechaUltimoReto = LocalDate.now()
        )
        every { usuarioRepository.findByIdFirebase(idFirebase) } returns usuarioMock

        val usuario = serviceBase.obtenerUsuario(idFirebase)
        assertEquals(usuarioMock, usuario)
    }

    @Test
    fun obtenerUsuario_usuarioNoEncontrado_lanzaNotFoundException() {
        val idFirebase = "no-existe"
        every { usuarioRepository.findByIdFirebase(idFirebase) } returns null

        val ex = assertThrows<NotFoundException> {
            serviceBase.obtenerUsuario(idFirebase)
        }
        assertEquals("Not found exception (404). Usuario no encontrado", ex.message)
    }

    @Test
    fun getFirebaseAuthInstance() {
        val authMock = mockk<FirebaseAuth>()
        val serviceSpy = io.mockk.spyk(serviceBase)

        every { serviceSpy.getFirebaseAuthInstance() } returns authMock

        val result = serviceSpy.getFirebaseAuthInstance()
        assertEquals(authMock, result)
    }

    @Test
    fun createHttpClient() {
        val client = serviceBase.createHttpClient()
        assertNotNull(client)
        assertTrue(client is HttpClient)
    }

    @Test
    fun getUsuarioRepository() {
        assertEquals(usuarioRepository, serviceBase.usuarioRepository)
    }
}