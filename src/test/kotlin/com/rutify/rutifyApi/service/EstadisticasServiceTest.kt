package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.EstadisticasDto
import com.rutify.rutifyApi.dto.EstadisticasPatchDto
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.IEstadisticasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import io.mockk.*
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import java.time.LocalDate
import kotlin.test.Test

class EstadisticasServiceTest {

    private val estadisticasRepository = mockk<IEstadisticasRepository>(relaxed = true)
    private val usuarioRepository = mockk<IUsuarioRepository>(relaxed = true)
    private var service: EstadisticasService= EstadisticasService(estadisticasRepository, usuarioRepository)
    private val authentication = mockk<Authentication>()

    @BeforeEach
    fun setUp() {
        mockkObject(DTOMapper)
    }

    val idFirebase = "user-123"

    val estadisticas = Estadisticas(
        "",idFirebase,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0.0
    )
    val estidisticasDto = EstadisticasDto(
        idFirebase,0.0,0.0,0.0,0.0,0.0,0,0.0
    )

    @Test
    fun `crearEstadisticas exitoso`() {
        every { authentication.name } returns idFirebase
        every { estadisticasRepository.findByIdFirebase(any()) } returns null
        every { estadisticasRepository.save(any()) } returns estadisticas
        every { DTOMapper.estadisticasToEstadisticasDto(any()) } returns estidisticasDto

        val response: ResponseEntity<EstadisticasDto> = service.crearEstadisticas(estadisticas, authentication)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(idFirebase, response.body?.idFirebase)

        verifySequence {
            authentication.name
            estadisticasRepository.findByIdFirebase(idFirebase)
            estadisticasRepository.save(estadisticas)
            DTOMapper.estadisticasToEstadisticasDto(estadisticas)
        }
    }

    @Test
    fun `crearEstadisticas lanza ConflictException si ya existe estadisticas`() {

        every { authentication.name } returns idFirebase
        every { estadisticasRepository.findByIdFirebase(idFirebase) } returns estadisticas

        val exception = assertThrows<ConflictException> {
            service.crearEstadisticas(estadisticas, authentication)
        }

        assertEquals("Conflicto (409). ya existe una estadisticas con el mismo id", exception.message)

        verify(exactly = 1) { estadisticasRepository.findByIdFirebase(idFirebase) }
        verify(exactly = 0) { estadisticasRepository.save(any()) }
    }

    @Test
    fun `obtenerEstadisticasPorUsuarioId exitoso`() {
        val usuarioId = "user-123"

        every { estadisticasRepository.findByIdFirebase(usuarioId) } returns estadisticas
        every { DTOMapper.estadisticasToEstadisticasDto(estadisticas) } returns estidisticasDto

        val response = service.obtenerEstadisticasPorUsuarioId(usuarioId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(estidisticasDto, response.body)

        verifySequence {
            estadisticasRepository.findByIdFirebase(usuarioId)
            DTOMapper.estadisticasToEstadisticasDto(estadisticas)
        }
    }

    @Test
    fun `obtenerEstadisticasPorUsuarioId lanza NotFoundException si no existe`() {
        val usuarioId = "user-123"

        every { estadisticasRepository.findByIdFirebase(usuarioId) } returns null

        val exception = assertThrows<NotFoundException> {
            service.obtenerEstadisticasPorUsuarioId(usuarioId)
        }

        assertEquals("Not found exception (404). No se encontraron estadísticas para el usuario con ID: $usuarioId", exception.message)

        verify(exactly = 1) { estadisticasRepository.findByIdFirebase(usuarioId) }
        verify(exactly = 0) { DTOMapper.estadisticasToEstadisticasDto(any()) }
    }

    @Test
    fun `actualizarEstadisticas exitoso`() {
        val usuarioId = "user-123"
        val auth = mockk<Authentication> {
            every { name } returns usuarioId
        }
        val patch = EstadisticasPatchDto(lvlBrazo = 3.0, horasActivo = 2.5)

        every { estadisticasRepository.findByIdFirebase(usuarioId) } returns estadisticas
        every { estadisticasRepository.save(any()) } answers { firstArg() }
        every { DTOMapper.estadisticasToEstadisticasDto(any()) } returns estidisticasDto

        val response = service.actualizarEstadisticas(usuarioId, patch, auth)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(estidisticasDto, response.body)

        verify {
            estadisticasRepository.findByIdFirebase(usuarioId)
            estadisticasRepository.save(withArg {
                assertEquals(patch.lvlBrazo, it.lvlBrazo)
                assertEquals(patch.horasActivo, it.horasActivo)
            })
            DTOMapper.estadisticasToEstadisticasDto(any())
        }
    }

    @Test
    fun `actualizarEstadisticas lanza UnauthorizedException si usuario no coincide`() {
        val usuarioId = "user-123"
        val auth = mockk<Authentication> {
            every { name } returns "otro-usuario"
        }
        val patch = EstadisticasPatchDto()

        val exception = assertThrows<UnauthorizedException> {
            service.actualizarEstadisticas(usuarioId, patch, auth)
        }

        assertEquals("Unauthorized (401). no tienes permiso para esa accion", exception.message)

        verify(exactly = 0) { estadisticasRepository.findByIdFirebase(any()) }
        verify(exactly = 0) { estadisticasRepository.save(any()) }
    }

    @Test
    fun `actualizarEstadisticas lanza NotFoundException si no existen estadísticas`() {
        val usuarioId = "user-123"
        val auth = mockk<Authentication> {
            every { name } returns usuarioId
        }
        val patch = EstadisticasPatchDto()

        every { estadisticasRepository.findByIdFirebase(usuarioId) } returns null

        val exception = assertThrows<NotFoundException> {
            service.actualizarEstadisticas(usuarioId, patch, auth)
        }

        assertEquals("Not found exception (404). Estadísticas no encontradas", exception.message)

        verify { estadisticasRepository.findByIdFirebase(usuarioId) }
        verify(exactly = 0) { estadisticasRepository.save(any()) }
    }

    @Test
    fun `eliminarEstadisticasPorUsuarioId exitoso cuando usuario es dueño`() {
        val usuarioId = "user-123"
        val auth = mockk<Authentication> {
            every { name } returns usuarioId
        }
        val usuario =  Usuario("otro-usuario","", LocalDate.now(),"","", LocalDate.now(), ObjectId(),"",false,false,0,"user")

        every { usuarioRepository.findByIdFirebase(usuarioId) } returns usuario
        every { estadisticasRepository.deleteAllByIdFirebase(usuarioId) } just Runs

        service.eliminarEstadisticasPorUsuarioId(usuarioId, auth)

        verify {
            usuarioRepository.findByIdFirebase(usuarioId)
            estadisticasRepository.deleteAllByIdFirebase(usuarioId)
        }
    }

    @Test
    fun `eliminarEstadisticasPorUsuarioId exitoso cuando usuario es admin`() {
        val usuarioId = "user-123"
        val auth = mockk<Authentication> {
            every { name } returns "admin-1"
        }
        val usuarioAdmin =  Usuario("admin-1","", LocalDate.now(),"","", LocalDate.now(), ObjectId(),"",false,false,0,"admin")

        every { usuarioRepository.findByIdFirebase("admin-1") } returns usuarioAdmin
        every { estadisticasRepository.deleteAllByIdFirebase(usuarioId) } just Runs

        service.eliminarEstadisticasPorUsuarioId(usuarioId, auth)

        verify {
            usuarioRepository.findByIdFirebase("admin-1")
            estadisticasRepository.deleteAllByIdFirebase(usuarioId)
        }
    }

    @Test
    fun `eliminarEstadisticasPorUsuarioId lanza NotFoundException si usuario autenticado no existe`() {
        val auth = mockk<Authentication> {
            every { name } returns "user-no-existe"
        }
        val usuarioId = "user-123"

        every { usuarioRepository.findByIdFirebase("user-no-existe") } returns null

        val exception = assertThrows<NotFoundException> {
            service.eliminarEstadisticasPorUsuarioId(usuarioId, auth)
        }

        assertEquals("Not found exception (404). usuario no encontrado", exception.message)

        verify { usuarioRepository.findByIdFirebase("user-no-existe") }
        verify(exactly = 0) { estadisticasRepository.deleteAllByIdFirebase(any()) }
    }

    @Test
    fun `eliminarEstadisticasPorUsuarioId lanza UnauthorizedException si usuario no es admin ni dueño`() {
        val usuarioId = "user-123"
        val auth = mockk<Authentication> {
            every { name } returns "otro-usuario"
        }
        val usuarioNoAdmin = Usuario("otro-usuario","", LocalDate.now(),"","", LocalDate.now(), ObjectId(),"",false,false,0,"user")

        every { usuarioRepository.findByIdFirebase("otro-usuario") } returns usuarioNoAdmin

        val exception = assertThrows<UnauthorizedException> {
            service.eliminarEstadisticasPorUsuarioId(usuarioId, auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso para aprobar comentarios", exception.message)

        verify {
            usuarioRepository.findByIdFirebase("otro-usuario")
        }
        verify(exactly = 0) { estadisticasRepository.deleteAllByIdFirebase(any()) }
    }

    @Test
    fun `findByIdFirebase retorna null cuando no existe`() {
        val idFirebase = "user-no-existe"

        every { estadisticasRepository.findByIdFirebase(idFirebase) } returns null

        val resultado = service.findByIdFirebase(idFirebase)

        assertNull(resultado)
    }

    private val servicespyk = spyk(service)

    @Test
    fun `deleteByIdUsuario delega a eliminarEstadisticasPorUsuarioId`() {
        val idFirebase = "user-123"
        val authentication = mockk<Authentication>()

        every { servicespyk.eliminarEstadisticasPorUsuarioId(any(), any()) } just Runs

        servicespyk.deleteByIdUsuario(idFirebase, authentication)

        verify(exactly = 1) { servicespyk.eliminarEstadisticasPorUsuarioId(idFirebase, authentication) }
    }

}