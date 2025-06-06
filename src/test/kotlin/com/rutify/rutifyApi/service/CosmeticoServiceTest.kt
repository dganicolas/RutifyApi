package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.repository.CosmeticoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class CosmeticoServiceTest{
    private val cosmeticoRepository = mockk<CosmeticoRepository>(relaxed = true)
    private lateinit var cosmeticoService: CosmeticoService

    @BeforeEach
    fun setUp() {
        cosmeticoService = CosmeticoService(cosmeticoRepository)
    }

    @Test
    fun `obtenerTodos devuelve lista de cosmeticos`() {
        val cosmeticosMock = listOf(
            Cosmetico(_id = "1", tipo = "tipo1", imagenUrl = "url1", nombre = "Cosmetico 1", precioMonedas = 100),
            Cosmetico(_id = "2", tipo = "tipo2", imagenUrl = "url2", nombre = "Cosmetico 2", precioMonedas = 200)
        )

        every { cosmeticoRepository.findAll() } returns cosmeticosMock

        val resultado = cosmeticoService.obtenerTodos()

        assertEquals(2, resultado.size)
        assertEquals("Cosmetico 1", resultado[0].nombre)
        assertEquals("Cosmetico 2", resultado[1].nombre)

        verify(exactly = 1) { cosmeticoRepository.findAll() }
    }
}