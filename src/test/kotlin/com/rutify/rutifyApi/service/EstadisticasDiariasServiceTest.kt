package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.EstadisticasDiarias
import com.rutify.rutifyApi.domain.Indumentaria
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.EstadisticasDiariasDto
import com.rutify.rutifyApi.dto.EstadisticasDiariasPatchDto
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.IEstadisticasDiariasRepository
import com.rutify.rutifyApi.utils.DTOMapper.estadisticasDiariasToDto
import io.mockk.*
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDate
import kotlin.test.Test

class EstadisticasDiariasServiceTest{
    private val estadisticasDiariasRepository = mockk<IEstadisticasDiariasRepository>(relaxed = true)
    private lateinit var service: EstadisticasDiariasService
    val estadisticas = EstadisticasDiarias(
        idFirebase = "user-123",
        fecha = LocalDate.of(2025, 6, 6),
        horasActivo = 0.0,
        pesoCorporal = 0.0,
        ejerciciosRealizados = 1,
        kCaloriasQuemadas = 0.0
    )
    val idFirebase = "user-123"
    val fecha = LocalDate.of(2025, 6, 6)
    @BeforeEach
    fun setUp() {
        service = EstadisticasDiariasService(estadisticasDiariasRepository)
    }

    @Test
    fun `obtenerEstadisticasDiariasDia retorna estadisticas cuando existen`() {

        val estadisticasDto = estadisticasDiariasToDto(estadisticas)

        every { estadisticasDiariasRepository.findByIdFirebaseAndFecha(any(), any()) } returns estadisticas

        val response: ResponseEntity<EstadisticasDiariasDto?> = service.obtenerEstadisticasDiariasDia("", LocalDate.now())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(estadisticasDto, response.body)

        verify(exactly = 1) { estadisticasDiariasRepository.findByIdFirebaseAndFecha(any(), any()) }
    }

    @Test
    fun `obtenerEstadisticasDiariasDia lanza NotFoundException cuando no existen`() {

        every { estadisticasDiariasRepository.findByIdFirebaseAndFecha(idFirebase, fecha) } returns null

        val exception = assertThrows<NotFoundException> {
            service.obtenerEstadisticasDiariasDia(idFirebase, fecha)
        }

        assertEquals("Not found exception (404). Estadisticas diarias no existen", exception.message)
        verify(exactly = 1) { estadisticasDiariasRepository.findByIdFirebaseAndFecha(idFirebase, fecha) }
    }

    @Test
    fun `findByIdFirebaseAndFecha crea o actualiza estadisticas correctamente`() {
        val patch = EstadisticasDiariasPatchDto(
            horasActivo = 1.5,
            ejerciciosRealizados = 2,
            kCaloriasQuemadas = 100.0,
            pesoCorporal = 70.0
        )

        // Mock: existe registro previo
        every { estadisticasDiariasRepository.findByIdFirebaseAndFecha(any(), any()) } returns estadisticas
        // Mock: guardado devuelve la misma entidad actualizada
        every { estadisticasDiariasRepository.save(any()) } answers { firstArg() }

        val response: ResponseEntity<EstadisticasDiariasDto> = service.findByIdFirebaseAndFecha(idFirebase, fecha, patch)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(70.0, response.body?.pesoCorporal)
        // Puedes agregar más asserts sobre las sumas

        verify(exactly = 1) { estadisticasDiariasRepository.findByIdFirebaseAndFecha(any(), any()) }
        verify(exactly = 1) { estadisticasDiariasRepository.save(any()) }
    }
    @Test
    fun `findByIdFirebaseAndFecha crea nuevo registro con pesoAnterior 0 cuando no hay datos previos`() {
        val patch = EstadisticasDiariasPatchDto(
            horasActivo = null,
            ejerciciosRealizados = null,
            kCaloriasQuemadas = null,
            pesoCorporal = null
        )

        // No hay registro existente
        every { estadisticasDiariasRepository.findByIdFirebaseAndFecha(any(), any()) } returns null
        // No hay registro anterior
        every { estadisticasDiariasRepository.findTopByIdFirebaseAndFechaBeforeOrderByFechaDesc(any(), any()) } returns null
        every { estadisticasDiariasRepository.save(any()) } answers { firstArg() }

        val response = service.findByIdFirebaseAndFecha(idFirebase, fecha, patch)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(0.0, response.body?.pesoCorporal) // pesoAnterior = 0.0 por default

        verify(exactly = 1) { estadisticasDiariasRepository.findByIdFirebaseAndFecha(idFirebase, fecha) }
        verify(exactly = 1) { estadisticasDiariasRepository.findTopByIdFirebaseAndFechaBeforeOrderByFechaDesc(idFirebase, fecha) }
        verify(exactly = 1) { estadisticasDiariasRepository.save(any()) }
    }

    @Test
    fun `obtenerUltimos5Pesos retorna lista con 5 pesos, rellenando con ceros si es necesario`() {
        // Simulamos que hay 3 registros recientes con pesos
        val registros = listOf(
            estadisticas,
            estadisticas,
            estadisticas
        )

        every { estadisticasDiariasRepository.findTop5ByIdFirebase(any()) } returns registros

        val response = service.obtenerUltimos5Pesos(idFirebase)
        val pesos = response.body!!

        // La lista debe tener 5 elementos: 3 pesos reales + 2 ceros al inicio, invertida
        assertEquals(5, pesos.size)
        assertEquals(listOf(0.0, 0.0, 0.0, 0.0, 0.0), pesos)

        assertEquals(HttpStatus.OK, response.statusCode)

        verify(exactly = 1) { estadisticasDiariasRepository.findTop5ByIdFirebase(idFirebase) }
    }

    @Test
    fun `eliminarEstadisticas exitoso si es usuario o admin`() {
        val idFirebase = "user-123"
        val usuarioMismo = Usuario(idFirebase = "otro-usuario", "user",
            LocalDate.now(),"","", LocalDate.now(), ObjectId(),"",false,false,1,"user",1,
            Indumentaria("","","",""))
        val usuarioAdmin = Usuario(idFirebase = "otro-usuario", "admin",
            LocalDate.now(),"","", LocalDate.now(), ObjectId(),"",false,false,1,"user",1,
            Indumentaria("","","",""))

        every { estadisticasDiariasRepository.deleteAllByIdFirebase(any()) } just runs

        val exception = assertThrows<UnauthorizedException> {
            service.eliminarEstadisticas(idFirebase, usuarioMismo)
        }

        assertEquals("Unauthorized (401). No tienes permiso para aprobar comentarios",exception.message)

    }

    @Test
    fun `eliminarEstadisticas lanza UnauthorizedException si usuario no tiene permiso`() {
        val idFirebase = "user-123"
        val usuarioNoAutorizado = Usuario(idFirebase = "otro-usuario", "user",
            LocalDate.now(),"","", LocalDate.now(), ObjectId(),"",false,false,1,"user",1,
            Indumentaria("","","","")
        )

        val exception = assertThrows<UnauthorizedException> {
            service.eliminarEstadisticas(idFirebase, usuarioNoAutorizado)
        }

        assertEquals("Unauthorized (401). No tienes permiso para aprobar comentarios", exception.message)
        verify(exactly = 0) { estadisticasDiariasRepository.deleteAllByIdFirebase(any()) }
    }
}