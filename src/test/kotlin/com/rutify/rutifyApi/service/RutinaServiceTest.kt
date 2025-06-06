package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Ejercicio
import com.rutify.rutifyApi.domain.Rutina
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.EjercicioDTO
import com.rutify.rutifyApi.dto.RutinaBuscadorDto
import com.rutify.rutifyApi.dto.RutinaDTO
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEjercicioRepository
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper
import com.rutify.rutifyApi.utils.DTOMapper.rutinasDtoToRutina
import io.mockk.*
import org.assertj.core.api.BDDAssertions.and
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import java.time.LocalDate
import java.util.*
import kotlin.test.Test

class RutinaServiceTest{
    private val rutinaRepository = mockk<IRutinasRepository>()
    private val ejercicioRepository = mockk<IEjercicioRepository>()
    private val usuarioRepository = mockk<IUsuarioRepository>()
    private val emailService = mockk<EmailService>()
    private val mongoTemplate = mockk<MongoTemplate>()

    private lateinit var rutinaService: RutinaService

    @BeforeEach
    fun setUp() {
        rutinaService = spyk(
            RutinaService(rutinaRepository, ejercicioRepository, usuarioRepository, emailService, mongoTemplate)
        )
    }

    @Test
    fun `crearRutina - caso bueno - rutina válida`() {
        val dto = RutinaDTO(
            id = "1",
            nombre = "Rutina Full Body",
            descripcion = "Descripción",
            creadorId = "user123",
            imagen = "",
            esPremium = false,
            ejercicios = listOf(
            )
        )

        val ejerciciosMap = mapOf("ej1" to 3)
        mockkObject(DTOMapper)
        every { rutinaService.validarRutina(any()) } just Runs
        every { rutinaService.obtenerIdDeEjercicio("ej1", "Sentadillas") } returns "ej1"
        every { rutinasDtoToRutina(dto, ejerciciosMap) } returns mockk()
        every { rutinaRepository.save(any()) } returns mockk()

        val response = rutinaService.crearRutina(dto)

        assert(response.statusCode == HttpStatus.CREATED)
        assert(response.body == dto)
    }

    @Test
    fun `crearRutina - caso malo - validación lanza excepción`() {
        val dto = RutinaDTO(
            id = "1",
            nombre = "Rutina Inválida",
            descripcion = "",
            creadorId = "user123",
            imagen = "",
            esPremium = false,
            ejercicios = emptyList()
        )

        every { rutinaService["validarRutina"](dto) } throws IllegalArgumentException("Datos inválidos")

        val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            rutinaService.crearRutina(dto)
        }

        assert(exception.message!!.contains("Datos inválidos"))
        verify(exactly = 0) { rutinaRepository.save(any()) }
    }

    @Test
    fun `obtenerIdDeEjercicio - caso bueno - devuelve id del ejercicio`() {
        val ejercicio = mockk<Ejercicio>()
        every { ejercicio.id } returns "ej1"
        every { ejercicioRepository.findById("ej1") } returns Optional.of(ejercicio)

        val result = rutinaService.obtenerIdDeEjercicio("ej1", "Sentadillas") as String

        assert(result == "ej1")
    }

    @Test
    fun `obtenerIdDeEjercicio - caso malo - ejercicio no encontrado`() {
        every { ejercicioRepository.findById("ej2") } returns Optional.empty()

        val exception = assertThrows<NotFoundException> {
            rutinaService.obtenerIdDeEjercicio("ej2", "Flexiones")
        }

        assert(exception.message!!.contains("Flexiones"))
    }

    @Test
    fun `obtenerIdDeEjercicio - caso malo - id del ejercicio es null`() {
        val ejercicio = mockk<Ejercicio>()
        every { ejercicio.id } returns null
        every { ejercicioRepository.findById("ej3") } returns Optional.of(ejercicio)

        val exception = assertThrows<ValidationException> {
            rutinaService.obtenerIdDeEjercicio("ej3", "Burpees")
        }

        assert(exception.message!!.contains("no tiene ID"))
    }

    @Test
    fun `validarRutina - caso bueno - no lanza excepcion`() {
        val dto = RutinaDTO(
            nombre = "Rutina de fuerza",
            descripcion = "Ejercicios para hipertrofia",
            ejercicios = listOf(EjercicioDTO("","","","","","",0.0,2.0,2)),
            creadorId = "user123",
            imagen = "",
            esPremium = false
        )

        assertDoesNotThrow {
            rutinaService.validarRutina(dto)
        }
    }

    @Test
    fun `validarRutina - caso malo - nombre vacio`() {
        val dto = RutinaDTO(
            nombre = " ",
            descripcion = "Buena rutina",
            ejercicios = listOf(EjercicioDTO("","","","","","",0.0,2.0,2)),
            creadorId = "abc123",
            imagen = "",
            esPremium = false
        )

        val ex = assertThrows<ValidationException> {
            rutinaService.validarRutina(dto)
        }

        assertEquals("Error en la validacion (400). El nombre no puede estar vacío", ex.message)
    }

    @Test
    fun `validarRutina - caso malo - descripcion vacia`() {
        val dto = RutinaDTO(
            nombre = "Rutina A",
            descripcion = "",
            ejercicios = listOf(),
            creadorId = "userX",
            imagen = "",
            esPremium = false
        )

        val ex = assertThrows<ValidationException> {
            rutinaService.validarRutina(dto)
        }

        assertEquals("Error en la validacion (400). La descripción no puede estar vacía", ex.message)
    }

    @Test
    fun `validarRutina - caso malo - sin ejercicios`() {
        val dto = RutinaDTO(
            nombre = "Rutina sin ejercicios",
            descripcion = "Faltan ejercicios",
            ejercicios = emptyList(),
            creadorId = "userY",
            imagen = "",
            esPremium = false
        )

        val ex = assertThrows<ValidationException> {
            rutinaService.validarRutina(dto)
        }

        assertEquals("Error en la validacion (400). La rutina debe tener al menos un ejercicio", ex.message)
    }

    @Test
    fun `validarRutina - caso malo - creadorId vacio`() {
        val dto = RutinaDTO(
            nombre = "Rutina de abdomen",
            descripcion = "Fortalece el core",
            ejercicios = listOf(EjercicioDTO("","","","","","",0.0,2.0,2)),
            creadorId = " ",
            imagen = "",
            esPremium = false
        )

        val ex = assertThrows<ValidationException> {
            rutinaService.validarRutina(dto)
        }

        assertEquals("Error en la validacion (400). El ID del creador no puede estar vacío", ex.message)
    }

    @Test
    fun `crearQueryPersonalizada - caso bueno - con filtro equipo`() {
        val query = rutinaService.crearQueryPersonalizada("Real Madrid", page = 1, size = 10)

        val criteria = query.queryObject["$and"] as? List<*> ?: emptyList<Any>()
        val filter = criteria.firstOrNull() as? org.bson.Document

        assertFalse(filter.toString().contains("equipo"))
        assertTrue(query.sortObject.containsKey("id"))
        assertEquals(10, query.limit)
        assertEquals(10, query.skip) // page 1 * size 10
    }

    @Test
    fun `obtenerRutinasBuscador - caso bueno - devuelve lista de resultados`() {
        // Arrange
        val equipo = "mancuernas"
        val page = 0
        val size = 2

        val mockRutina = Rutina(
            id = "rut1",
            nombre = "Rutina A",
            descripcion = "Fuerza",
            creadorId = "uid123",
            ejercicios = mapOf("ej1" to 3),
            equipo = "mancuernas",
            imagen = "",
            esPremium = false
        )

        every { mongoTemplate.find(any<Query>(), Rutina::class.java) } returns listOf(mockRutina)

        // Act
        val response = rutinaService.obtenerRutinasBuscador(page, size, equipo)

        // Assert
        assertEquals(200, response.statusCode.value())
        val body = response.body!!
        assertEquals(1, body.size)
        assertEquals("Rutina A", body[0].nombre)
        assertEquals("Fuerza", body[0].descripcion)
    }

    @Test
    fun `obtenerRutinasPorAutor - caso bueno - devuelve lista de rutinas`() {
        // Arrange
        val creadorId = "usuario123"

        val mockRutina = Rutina(
            id = "rut1",
            nombre = "Rutina Fuerza",
            descripcion = "Entrenamiento básico",
            creadorId = creadorId,
            ejercicios = mapOf("ej1" to 3),
            equipo = "pesas",
            esPremium = true,
            imagen = ""
        )

        every { rutinaRepository.findAllByCreadorId(creadorId) } returns listOf(mockRutina)

        // Act
        val response: ResponseEntity<List<RutinaBuscadorDto>> = rutinaService.obtenerRutinasPorAutor(creadorId)

        // Assert
        assertEquals(200, response.statusCode.value())
        val listaRutinas = response.body!!
        assertEquals(1, listaRutinas.size)
        assertEquals("Rutina Fuerza", listaRutinas[0].nombre)
    }

    @Test
    fun `obtenerRutinasPorAutor - caso malo - id creador vacio lanza ValidationException`() {
        // Arrange
        val creadorId = ""

        // Act & Assert
        val exception = assertThrows<ValidationException> {
            rutinaService.obtenerRutinasPorAutor(creadorId)
        }
        assertEquals("Error en la validacion (400). El ID del creador no puede estar vacío", exception.message)
    }

    @Test
    fun `eliminarTodasRutinasDelusuario - caso bueno - usuario admin o mismo id elimina rutinas`() {
        // Arrange
        val idFirebase = "usuario1"
        val auth = mockk<Authentication>()
        every { auth.name } returns "usuario1" // autenticado mismo usuario

        val usuario = Usuario(
            idFirebase = "usuario1",
            sexo = "M",
            fechaNacimiento = LocalDate.now().minusYears(30),
            nombre = "Usuario Uno",
            correo = "usuario1@email.com",
            fechaUltimoReto = LocalDate.now(),
            esPremium = false,
            rol = "user"
        )

        val rutinasDto = listOf(
            RutinaBuscadorDto(id = "rut1", nombre = "Rutina A","","",1,1f,1,false, equipo = ""),
            RutinaBuscadorDto(id = "rut2", nombre = "Rutina B","","",1,1f,1,false, equipo = ""),
        )

        // Mock obtenerUsuario para devolver el usuario
        every { rutinaService.obtenerUsuario(any()) } returns usuario

        // Mock obtenerRutinasPorAutor para devolver la lista de rutinas
        every { rutinaService.obtenerRutinasPorAutor(any()) } returns ResponseEntity.ok(rutinasDto)

        // Mock eliminarRutina para no hacer nada (void)
        every { rutinaService.eliminarRutina(any(), auth) } returns ResponseEntity.noContent().build()

        // Act
        rutinaService.eliminarTodasRutinasDelusuario(idFirebase, auth)

        // Assert
        verify(exactly = 1) { rutinaService.obtenerUsuario("usuario1") }
        verify(exactly = 1) { rutinaService.obtenerRutinasPorAutor("usuario1") }
        verify(exactly = 2) { rutinaService.eliminarRutina(any(), auth) }
    }

    @Test
    fun `eliminarTodasRutinasDelusuario - caso malo - usuario no admin y distinto id lanza UnauthorizedException`() {
        // Arrange
        val idFirebase = "usuario2"
        val auth = mockk<Authentication>()
        every { auth.name } returns "usuario1" // usuario autenticado distinto id

        val usuario = Usuario(
            idFirebase = "usuario1",
            sexo = "M",
            fechaNacimiento = LocalDate.now().minusYears(30),
            nombre = "Usuario Uno",
            correo = "usuario1@email.com",
            fechaUltimoReto = LocalDate.now(),
            esPremium = false,
            rol = "user" // NO es admin
        )

        every { rutinaService.obtenerUsuario("usuario1") } returns usuario

        // Act & Assert
        val exception = assertThrows<UnauthorizedException> {
            rutinaService.eliminarTodasRutinasDelusuario(idFirebase, auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso para aprobar comentarios", exception.message)
    }

    @Test
    fun `obtenerRutinaPorId caso bueno - devuelve rutina con ejercicios DTO`() {
        val idRutina = "rut1"
        val rutina = Rutina(
            id = idRutina,
            ejercicios = mapOf("ej1" to 10, "ej2" to 15),
            nombre = "",
            descripcion = "",
            equipo = "",
            esPremium = false,
            reportes = 1,
            creadorId = "",
            imagen = ""
        )
        val ejercicio1 = Ejercicio(id = "ej1", "","","","","",0.0,0.0)
        val ejercicio2 = Ejercicio(id = "ej2", "","","","","",0.0,0.0)
        val ejercicios = listOf(ejercicio1, ejercicio2)

        every { rutinaRepository.findById(idRutina) } returns Optional.of(rutina)
        every { ejercicioRepository.findAllById(rutina.ejercicios.keys.toList()) } returns ejercicios

        val response: ResponseEntity<RutinaDTO> = rutinaService.obtenerRutinaPorId(idRutina)

        assertEquals(200, response.statusCodeValue)
        // Aquí puedes agregar más asserts para validar el contenido de response.body si quieres

        verify(exactly = 1) { rutinaRepository.findById(idRutina) }
        verify(exactly = 1) { ejercicioRepository.findAllById(rutina.ejercicios.keys.toList()) }
    }

    @Test
    fun `obtenerRutinaPorId caso malo - rutina no encontrada lanza NotFoundException`() {
        val idRutina = "rut-no-existe"

        every { rutinaRepository.findById(idRutina) } returns Optional.empty()

        val exception = assertThrows<NotFoundException> {
            rutinaService.obtenerRutinaPorId(idRutina)
        }

        assertEquals("Not found exception (404). No se encontró la rutina con ID: $idRutina", exception.message)

        verify(exactly = 1) { rutinaRepository.findById(idRutina) }
        // ejercicioRepository no debería llamarse
    }

    @Test
    fun `eliminarRutina caso bueno - elimina rutina con permisos`() {
        val idRutina = "rut1"
        val auth = mockk<Authentication>()
        every { auth.name } returns "usuario1"

        val usuario = Usuario(
            idFirebase = "usuario1",
            sexo = "M",
            fechaNacimiento = LocalDate.now(),
            nombre = "Usuario Uno",
            correo = "usuario1@email.com",
            fechaUltimoReto = LocalDate.now(),
            esPremium = false,
            rol = "user"
        )
        val rutina = Rutina(
            id = idRutina,
            ejercicios = mapOf("ej1" to 10, "ej2" to 15),
            nombre = "Rutina Ejemplo",
            descripcion = "Descripcion",
            equipo = "",
            esPremium = false,
            reportes = 1,
            creadorId = "usuario1",
            imagen = ""
        )

        every { usuarioRepository.findByIdFirebase("usuario1") } returns usuario
        every { rutinaRepository.findById(idRutina) } returns Optional.of(rutina)
        every { rutinaRepository.delete(rutina) } just runs
        every { emailService.enviarCorreoNotificacion(any(), any(), any()) } just runs

        val response = rutinaService.eliminarRutina(idRutina, auth)

        assertEquals(204, response.statusCodeValue)
        verify(exactly = 1) { usuarioRepository.findByIdFirebase("usuario1") }
        verify(exactly = 1) { rutinaRepository.findById(idRutina) }
        verify(exactly = 1) { rutinaRepository.delete(rutina) }
        verify(exactly = 0) { emailService.enviarCorreoNotificacion(any(), any(), any()) }
    }

    @Test
    fun `eliminarRutina caso bueno - admin elimina y se envía correo`() {
        val idRutina = "rut1"
        val auth = mockk<Authentication>()
        every { auth.name } returns "admin1"

        val usuario = Usuario(
            idFirebase = "admin1",
            sexo = "M",
            fechaNacimiento = LocalDate.now(),
            nombre = "Admin",
            correo = "admin@email.com",
            fechaUltimoReto = LocalDate.now(),
            esPremium = true,
            rol = "admin"
        )
        val rutina = Rutina(
            id = idRutina,
            ejercicios = mapOf("ej1" to 10, "ej2" to 15),
            nombre = "Rutina Ejemplo",
            descripcion = "Descripcion",
            equipo = "",
            esPremium = false,
            reportes = 1,
            creadorId = "otroUsuario",
            imagen = ""
        )

        every { usuarioRepository.findByIdFirebase("admin1") } returns usuario
        every { rutinaRepository.findById(idRutina) } returns Optional.of(rutina)
        every { rutinaRepository.delete(rutina) } just runs
        every { emailService.enviarCorreoNotificacion(usuario.correo, any(), any()) } just runs

        val response = rutinaService.eliminarRutina(idRutina, auth)

        assertEquals(204, response.statusCodeValue)
        verify(exactly = 1) { emailService.enviarCorreoNotificacion(usuario.correo, "rutina eliminada", "Hemos eliminado tu rutina ${rutina.nombre} por incumplimiento de la comunidad") }
    }

    @Test
    fun `eliminarRutina caso malo - usuario no encontrado lanza NotFoundException`() {
        val idRutina = "rut1"
        val auth = mockk<Authentication>()
        every { auth.name } returns "usuarioNoExiste"

        every { usuarioRepository.findByIdFirebase("usuarioNoExiste") } returns null

        val exception = assertThrows<NotFoundException> {
            rutinaService.eliminarRutina(idRutina, auth)
        }

        assertEquals("Not found exception (404). El usuario usuarioNoExiste no existe ", exception.message)
    }

    @Test
    fun `eliminarRutina caso malo - rutina no encontrada lanza NotFoundException`() {
        val idRutina = "rutNoExiste"
        val auth = mockk<Authentication>()
        every { auth.name } returns "usuario1"

        val usuario = Usuario(
            idFirebase = "usuario1",
            sexo = "M",
            fechaNacimiento = LocalDate.now(),
            nombre = "Usuario Uno",
            correo = "usuario1@email.com",
            fechaUltimoReto = LocalDate.now(),
            esPremium = false,
            rol = "user"
        )

        every { usuarioRepository.findByIdFirebase("usuario1") } returns usuario
        every { rutinaRepository.findById(idRutina) } returns Optional.empty()

        val exception = assertThrows<NotFoundException> {
            rutinaService.eliminarRutina(idRutina, auth)
        }

        assertEquals("Not found exception (404). No se encontró la rutina con ID: $idRutina", exception.message)
    }

    @Test
    fun `eliminarRutina caso malo - sin permisos lanza UnauthorizedException`() {
        val idRutina = "rut1"
        val auth = mockk<Authentication>()
        every { auth.name } returns "usuario2" // no es creador ni admin

        val usuario = Usuario(
            idFirebase = "usuario2",
            sexo = "M",
            fechaNacimiento = LocalDate.now(),
            nombre = "Usuario Dos",
            correo = "usuario2@email.com",
            fechaUltimoReto = LocalDate.now(),
            esPremium = false,
            rol = "user"
        )
        val rutina = Rutina(
            id = idRutina,
            ejercicios = mapOf("ej1" to 10, "ej2" to 15),
            nombre = "Rutina Ejemplo",
            descripcion = "Descripcion",
            equipo = "",
            esPremium = false,
            reportes = 1,
            creadorId = "usuario1", // otro creador
            imagen = ""
        )

        every { usuarioRepository.findByIdFirebase("usuario2") } returns usuario
        every { rutinaRepository.findById(idRutina) } returns Optional.of(rutina)

        val exception = assertThrows<UnauthorizedException> {
            rutinaService.eliminarRutina(idRutina, auth)
        }

        assertEquals("Unauthorized (401). No tienes permiso para crear este voto a otro usuario", exception.message)
    }

    @Test
    fun `buscarRutinas caso bueno - con nombre que retorna resultados`() {
        val nombreBusqueda = "Rut"

        val rutinas = listOf(
            Rutina(id = "r1", ejercicios = emptyMap(), nombre = "Rutina A", descripcion = "", equipo = "", esPremium = false, reportes = 0, creadorId = "", imagen = ""),
            Rutina(id = "r2", ejercicios = emptyMap(), nombre = "Rutina B", descripcion = "", equipo = "", esPremium = false, reportes = 0, creadorId = "", imagen = "")
        )

        every {
            mongoTemplate.find(
                any<Query>(),
                Rutina::class.java
            )
        } returns rutinas

        val response: ResponseEntity<List<RutinaBuscadorDto>> = rutinaService.buscarRutinas(nombreBusqueda)

        assertEquals(200, response.statusCodeValue)
        assertEquals(2, response.body?.size)
        assertTrue(response.body?.all { it.nombre.startsWith("Rut", ignoreCase = true) } == true)

        verify(exactly = 1) {
            mongoTemplate.find(any<Query>(), Rutina::class.java)
        }
    }
}