package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.dto.VotodDto
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.repository.IVotosRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.Test

class VotosServiceTest {

    private lateinit var rutinaRepository: IRutinasRepository
    private lateinit var votosRepository: IVotosRepository
    private lateinit var usuarioRepository: IUsuarioRepository
    private lateinit var votosService: VotosService

    @BeforeEach
    fun setup() {
        rutinaRepository = mockk()
        votosRepository = mockk()
        usuarioRepository = mockk()
        votosService = VotosService(rutinaRepository, votosRepository, usuarioRepository)
    }

    @Test
    fun `validarVotos caso bueno - usuario y rutina existen, puntuacion válida`() {
        val voto = VotodDto(idRutina = "rutina1", idFirebase = "user1", puntuacion = 4.5f, nombreRutina = "")

        every { usuarioRepository.findByIdFirebase("user1") } returns mockk() // usuario existe
        every { rutinaRepository.findById("rutina1") } returns Optional.of(mockk()) // rutina existe

        votosService.validarVotos(voto)

        verify(exactly = 1) { usuarioRepository.findByIdFirebase("user1") }
        verify(exactly = 1) { rutinaRepository.findById("rutina1") }
    }

    @Test
    fun `validarVotos falla cuando usuario no existe`() {
        val voto = VotodDto(idRutina = "rutina1", idFirebase = "userX", puntuacion = 3.0f, nombreRutina = "")

        every { usuarioRepository.findByIdFirebase(any()) } returns null
        every { rutinaRepository.findById(any()) } returns Optional.empty()
        val ex = assertThrows<ValidationException> {
            votosService.validarVotos(voto)
        }
        assertEquals("Error en la validacion (400). el usuario no existe", ex.message)
    }

    @Test
    fun `validarVotos falla cuando rutina no existe`() {
        val voto = VotodDto(idRutina = "rutinaX", idFirebase = "user1", puntuacion = 3.0f, nombreRutina = "")

        every { usuarioRepository.findByIdFirebase(any()) } returns mockk()
        every { rutinaRepository.findById(any()) } returns Optional.empty()

        val ex = assertThrows<ValidationException> {
            votosService.validarVotos(voto)
        }
        assertEquals("Error en la validacion (400). la rutina no existe", ex.message)
    }

    @Test
    fun `validarVotos falla cuando puntuacion es menor o igual a 0`() {
        val voto = VotodDto(idRutina = "rutina1", idFirebase = "user1", puntuacion = 0.0f, nombreRutina = "")

        every { usuarioRepository.findByIdFirebase(any()) } returns mockk()
        every { rutinaRepository.findById(any()) } returns Optional.of(mockk())

        val ex = assertThrows<ValidationException> {
            votosService.validarVotos(voto)
        }
        assertEquals("Error en la validacion (400). La puntuación debe ser mayor a 0.0 y menor a 5.0", ex.message)
    }

    @Test
    fun `validarVotos falla cuando puntuacion es mayor a 5`() {
        val voto = VotodDto(idRutina = "rutina1", idFirebase = "user1", puntuacion = 5.5f, nombreRutina = "")

        every { usuarioRepository.findByIdFirebase(any()) } returns mockk()
        every { rutinaRepository.findById(any()) } returns Optional.of(mockk())

        val ex = assertThrows<ValidationException> {
            votosService.validarVotos(voto)
        }
        assertEquals("Error en la validacion (400). La puntuación debe ser mayor a 0.0 y menor a 5.0", ex.message)
    }
}