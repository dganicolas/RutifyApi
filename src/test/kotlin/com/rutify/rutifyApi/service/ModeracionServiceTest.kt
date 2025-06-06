package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Comentario
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.ComentarioDto
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.ComentarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import io.mockk.*
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import java.time.LocalDate
import kotlin.test.Test

class ModeracionServiceTest{

    private val comentarioRepository = mockk<ComentarioRepository>()
    private val usuariosService = mockk<UsuariosService>()
    private val comunidadService = mockk<ComentarioService>()
    private val notificacionService = mockk<NotificacionService>()
    private val mensajesService = mockk<MensajesService>()

    private val moderacionService = ModeracionService(
        comentarioRepository,
        usuariosService,
        comunidadService,
        notificacionService,
        mensajesService
    )

    @Test
    fun `verificarModeracionImagenes retorna lista para admin`() {
        val auth = mockk<Authentication>()
        val userId = "adminUser"
        val comentario1 = mockk<Comentario>()
        val comentario2 = mockk<Comentario>()
        val dto1 = mockk<ComentarioDto>()
        val dto2 = mockk<ComentarioDto>()

        every { auth.name } returns userId
        every { usuariosService.EsAdmin(userId) } returns ResponseEntity.ok(true)
        every { comentarioRepository.findByEstadoIsFalse() } returns listOf(comentario1, comentario2)

        mockkObject(DTOMapper)
        every { DTOMapper.ComentarioToComentarioDto(comentario1) } returns dto1
        every { DTOMapper.ComentarioToComentarioDto(comentario2) } returns dto2

        val resultado = moderacionService.verificarModeracionImagenes(auth)

        assertEquals(2, resultado.size)
        assertEquals(dto1, resultado[0])
        assertEquals(dto2, resultado[1])

        verify(exactly = 1) { usuariosService.EsAdmin(userId) }
        verify(exactly = 1) { comentarioRepository.findByEstadoIsFalse() }
        verify { DTOMapper.ComentarioToComentarioDto(any()) }

        unmockkObject(DTOMapper)
    }

    @Test
    fun `verificarModeracionImagenes lanza UnauthorizedException para no admin`() {
        val auth = mockk<Authentication>()
        val userId = "normalUser"

        every { auth.name } returns userId
        every { usuariosService.EsAdmin(userId) } returns ResponseEntity.ok(false)

        val exception = assertThrows<UnauthorizedException> {
            moderacionService.verificarModeracionImagenes(auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso", exception.message)

        verify(exactly = 1) { usuariosService.EsAdmin(userId) }
        verify(exactly = 0) { comentarioRepository.findByEstadoIsFalse() }
    }

    @Test
    fun `eliminarComentario elimina comentario correctamente cuando usuario es admin`() {
        val auth = mockk<Authentication>()
        val userId = "adminUser"
        val comentarioId = "comentario123"

        val comentario = Comentario(
            _id = comentarioId,
            idFirebase = "usuario123",
            imagenUrl = "http://url.imagen",
            estadoAnimo = "",
            nombreUsuario = "",
            avatarUrl = "",
            fechaPublicacion = LocalDate.now(),
            texto = ""
        )

        every { auth.name } returns userId
        every { comunidadService.obtenerComentarioPorId(comentarioId) } returns comentario
        every { usuariosService.EsAdmin(userId) } returns ResponseEntity.ok(true)
        every { comunidadService.eliminarComentario(any(), auth) } just Runs

        every { mensajesService.obtenerMensaje(any()) } returns "Titulo Notificacion"
        every { mensajesService.obtenerMensaje(any()) } returns "Cuerpo Notificacion"
        every { mensajesService.obtenerMensaje(any(), arrayOf(comentario.imagenUrl)) } returns "Imagen eliminada log"
        every { notificacionService.incumplimiento(any(), allAny(),any()) } returns Unit

        moderacionService.eliminarComentario(comentarioId, auth)

        verify(exactly = 1) { comunidadService.obtenerComentarioPorId(comentarioId) }
        verify(exactly = 1) { usuariosService.EsAdmin(userId) }
        verify(exactly = 1) { comunidadService.eliminarComentario(comentarioId, auth) }
    }

    @Test
    fun `eliminarComentario lanza UnauthorizedException cuando usuario no es admin`() {
        val auth = mockk<Authentication>()
        val userId = "userNoAdmin"
        val comentarioId = "comentario123"

        val comentario = Comentario(
            _id = comentarioId,
            idFirebase = "usuario123",
            imagenUrl = "http://url.imagen",
            estadoAnimo = "",
            nombreUsuario = "",
            avatarUrl = "",
            fechaPublicacion = LocalDate.now(),
            texto = ""
        )

        every { auth.name } returns userId
        every { comunidadService.obtenerComentarioPorId(comentarioId) } returns comentario
        every { usuariosService.EsAdmin(userId) } returns ResponseEntity.ok(false)

        val exception = assertThrows<UnauthorizedException> {
            moderacionService.eliminarComentario(comentarioId, auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso", exception.message)

        verify(exactly = 1) { usuariosService.EsAdmin(userId) }
        verify(exactly = 0) { comunidadService.eliminarComentario(any(), any()) }
        verify(exactly = 0) { notificacionService.incumplimiento(any(), any(), any()) }
    }

    @Test
    fun `obtenerUsuariosReportados devuelve lista cuando usuario es admin`() {
        val auth = mockk<Authentication>()
        val adminId = "adminId"
        val usuariosReportados = listOf(
            Usuario(
                idFirebase = "user1",
                nombre = "Nombre1",
                sexo = "M",
                esPremium = true,
                avatar = "avatar1",
                reportes = 2,
                gimnasioId = ObjectId(),
                fechaUltimoReto = LocalDate.now(),
                fechaNacimiento = LocalDate.now(),
                rol = "",
                correo = ""
            ),
            Usuario(
                idFirebase = "user2",
                nombre = "Nombre2",
                sexo = "F",
                esPremium = false,
                avatar = "avatar2",
                reportes = 3,
                gimnasioId = ObjectId(),
                fechaUltimoReto = LocalDate.now(),
                fechaNacimiento = LocalDate.now(),
                rol = "",
                correo = ""
            )
        )

        every { auth.name } returns adminId
        every { usuariosService.EsAdmin(adminId) } returns ResponseEntity.ok(true)
        every { usuariosService.findByReportesGreaterThanOrderByReportesAsc() } returns usuariosReportados

        val resultado = moderacionService.obtenerUsuariosReportados(auth)

        assertEquals(2, resultado.size)
        assertEquals("user1", resultado[0].idFirebase)
        assertEquals("Nombre1", resultado[0].nombre)

        verify(exactly = 1) { usuariosService.EsAdmin(adminId) }
        verify(exactly = 1) { usuariosService.findByReportesGreaterThanOrderByReportesAsc() }
    }

    @Test
    fun `obtenerUsuariosReportados lanza UnauthorizedException cuando no es admin`() {
        val auth = mockk<Authentication>()
        val userId = "userId"

        every { auth.name } returns userId
        every { usuariosService.EsAdmin(userId) } returns ResponseEntity.ok(false)

        val exception = assertThrows<UnauthorizedException> {
            moderacionService.obtenerUsuariosReportados(auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso", exception.message)

        verify(exactly = 1) { usuariosService.EsAdmin(userId) }
        verify(exactly = 0) { usuariosService.findByReportesGreaterThanOrderByReportesAsc() }
    }

    @Test
    fun `eliminarUsuario - caso bueno - admin elimina correctamente`() {
        val auth = mockk<Authentication>()
        val userId = "usuario1"
        val correo = "test@correo.com"

        every { auth.name } returns "admin123"
        every { usuariosService.EsAdmin("admin123") } returns ResponseEntity.ok(true)
        every { usuariosService.obtenerUsuario(userId) } returns  Usuario(
            idFirebase = "user1",
            nombre = "Nombre1",
            sexo = "M",
            esPremium = true,
            avatar = "avatar1",
            reportes = 2,
            gimnasioId = ObjectId(),
            fechaUltimoReto = LocalDate.now(),
            fechaNacimiento = LocalDate.now(),
            rol = "",
            correo = ""
        )
        every { usuariosService.eliminarUsuarioPorCorreo(any(), auth, false) } returns ResponseEntity.ok().build()

        val response = moderacionService.eliminarUsuario(userId, auth)

        assertEquals(200, response.statusCode.value())
    }

    @Test
    fun `eliminarUsuario - caso malo - usuario no admin lanza UnauthorizedException`() {
        val auth = mockk<Authentication>()

        every { auth.name } returns "userNoAdmin"
        every { usuariosService.EsAdmin("userNoAdmin") } returns ResponseEntity.ok(false)

        val exception = assertThrows<UnauthorizedException> {
            moderacionService.eliminarUsuario("usuario1", auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso", exception.message)
    }
}