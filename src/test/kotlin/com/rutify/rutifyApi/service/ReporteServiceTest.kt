package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Reporte
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.repository.ReporteRepository
import io.mockk.*
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import java.time.LocalDate
import kotlin.test.Test

class ReporteServiceTest {

    private val reporteRepository = mockk<ReporteRepository>()
    private val usuarioRepository = mockk<IUsuarioRepository>()
    private val authentication = mockk<Authentication>()

    private lateinit var reporteService: ReporteService

    @BeforeEach
    fun setUp() {
        reporteService = ReporteService(reporteRepository, usuarioRepository)
        every { authentication.name } returns "reportador123"
    }

    @Test
    fun `reportarUsuario - caso bueno - usuario reportado correctamente`() {
        val reportadoId = "usuario456"
        val usuario = Usuario(
            idFirebase = reportadoId,
            correo = "test@correo.com",
            reportes = 0,
            gimnasioId = ObjectId(),
            fechaUltimoReto = LocalDate.now(),
            fechaNacimiento = LocalDate.now(),
            rol = "",
            sexo = "",
            nombre = "",
            esPremium = false
        )

        every { reporteRepository.findByReportadorIdFirebaseAndReportadoIdFirebase("reportador123", reportadoId) } returns null
        every { usuarioRepository.findByIdFirebase(reportadoId) } returns usuario
        every { usuarioRepository.save(usuario) } returns usuario
        every { reporteRepository.save(any()) } returns mockk()

        val response = reporteService.reportarUsuario(reportadoId, authentication)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Reporte enviado correctamente.", response.body)
        assertEquals(1, usuario.reportes)

        verify { reporteRepository.findByReportadorIdFirebaseAndReportadoIdFirebase("reportador123", reportadoId) }
        verify { usuarioRepository.findByIdFirebase(reportadoId) }
        verify { usuarioRepository.save(usuario) }
        verify { reporteRepository.save(any()) }
    }

    @Test
    fun `reportarUsuario - caso malo - ya se ha reportado al usuario`() {
        val reportadoId = "usuario456"
        val existente = Reporte("","reportador123", reportadoId)

        every { reporteRepository.findByReportadorIdFirebaseAndReportadoIdFirebase("reportador123", reportadoId) } returns existente

        val ex = assertThrows<ConflictException> {
            reporteService.reportarUsuario(reportadoId, authentication)
        }

        assertEquals("Conflicto (409). Ya reportaste a este usuario.", ex.message)
        verify { reporteRepository.findByReportadorIdFirebaseAndReportadoIdFirebase("reportador123", reportadoId) }
        verify(exactly = 0) { usuarioRepository.findByIdFirebase(any()) }
    }

    @Test
    fun `reportarUsuario - caso malo - usuario a reportar no existe`() {
        val reportadoId = "usuario-no-existe"

        every { reporteRepository.findByReportadorIdFirebaseAndReportadoIdFirebase("reportador123", reportadoId) } returns null
        every { usuarioRepository.findByIdFirebase(reportadoId) } returns null

        val ex = assertThrows<NotFoundException> {
            reporteService.reportarUsuario(reportadoId, authentication)
        }

        assertEquals("Not found exception (404). el ususario no existe", ex.message)
        verify { usuarioRepository.findByIdFirebase(reportadoId) }
        verify(exactly = 0) { reporteRepository.save(any()) }
    }

    @Test
    fun `eliminarReportes - caso bueno - el usuario se elimina a sí mismo`() {
        val id = "user123"
        val usuario = Usuario(idFirebase = id, rol = "user",
            correo = "test@correo.com",
            reportes = 0,
            gimnasioId = ObjectId(),
            fechaUltimoReto = LocalDate.now(),
            fechaNacimiento = LocalDate.now(),
            sexo = "",
            nombre = "",
            esPremium = false)

        every { authentication.name } returns id
        every { usuarioRepository.findByIdFirebase(id) } returns usuario
        every { reporteRepository.deleteAllByReportadorIdFirebase(id) } just Runs
        every { reporteRepository.deleteAllByReportadoIdFirebase(id) } just Runs

        reporteService.eliminarReportes(id, authentication)

        verify { reporteRepository.deleteAllByReportadorIdFirebase(id) }
        verify { reporteRepository.deleteAllByReportadoIdFirebase(id) }
    }

    @Test
    fun `eliminarReportes - caso bueno - un admin elimina a otro usuario`() {
        val admin = Usuario(idFirebase = "admin1", rol = "admin",
            correo = "test@correo.com",
            reportes = 0,
            gimnasioId = ObjectId(),
            fechaUltimoReto = LocalDate.now(),
            fechaNacimiento = LocalDate.now(),
            sexo = "",
            nombre = "",
            esPremium = false)
        val objetivo = "user456"

        every { authentication.name } returns "admin1"
        every { usuarioRepository.findByIdFirebase("admin1") } returns admin
        every { reporteRepository.deleteAllByReportadorIdFirebase(objetivo) } just Runs
        every { reporteRepository.deleteAllByReportadoIdFirebase(objetivo) } just Runs

        reporteService.eliminarReportes(objetivo, authentication)

        verify { reporteRepository.deleteAllByReportadorIdFirebase(objetivo) }
        verify { reporteRepository.deleteAllByReportadoIdFirebase(objetivo) }
    }

    @Test
    fun `eliminarReportes - caso malo - usuario no es admin y quiere eliminar a otro`() {
        val usuario = Usuario(idFirebase = "user123", rol = "user",
            correo = "test@correo.com",
            reportes = 0,
            gimnasioId = ObjectId(),
            fechaUltimoReto = LocalDate.now(),
            fechaNacimiento = LocalDate.now(),
            sexo = "",
            nombre = "",
            esPremium = false)

        every { authentication.name } returns "user123"
        every { usuarioRepository.findByIdFirebase("user123") } returns usuario

        val ex = assertThrows<UnauthorizedException> {
            reporteService.eliminarReportes("otroUser", authentication)
        }

        assert(ex.message!!.contains("No tienes permiso"))
        verify(exactly = 0) { reporteRepository.deleteAllByReportadorIdFirebase(any()) }
    }

    @Test
    fun `eliminarReportes - caso malo - usuario solicitante no existe`() {
        every { authentication.name } returns "ghostUser"
        every { usuarioRepository.findByIdFirebase("ghostUser") } returns null

        val ex = assertThrows<NotFoundException> {
            reporteService.eliminarReportes("otroUser", authentication)
        }

        assert(ex.message!!.contains("usuario no encontrado"))
        verify(exactly = 0) { reporteRepository.deleteAllByReportadorIdFirebase(any()) }
    }
}