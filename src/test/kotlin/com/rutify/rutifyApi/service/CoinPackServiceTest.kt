package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.CoinPack
import com.rutify.rutifyApi.repository.CoinPackRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class CoinPackServiceTest{
    private val repositoryMock = mockk<CoinPackRepository>()
    private val service = CoinPackService(repositoryMock)

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun obtenerPacksretornalistadepackscorrectamente() {
        // Arrange
        val listaEsperada = listOf(
            CoinPack(id = "1", nombre = "Pack Pequeño", monedas = 100, precio = 1.99),
            CoinPack(id = "2", nombre = "Pack Grande", monedas = 1000, precio = 9.99)
        )

        every { repositoryMock.findAll() } returns listaEsperada

        // Act
        val resultado = service.obtenerPacks()

        // Assert
        assertEquals(listaEsperada, resultado)
    }
}