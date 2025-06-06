package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Compra
import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.repository.CompraRepository
import com.rutify.rutifyApi.repository.CosmeticoRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.test.Test

class CompraServiceTest {


    // Mocks de dependencias para CompraService
    private val compraRepository = mockk<CompraRepository>(relaxed = true)
    private val cosmeticoRepository = mockk<CosmeticoRepository>(relaxed = true)
    private val usuarioService = mockk<UsuariosService>(relaxed = true)

    // Instancia real del servicio con las dependencias mockeadas
    private val compraService = CompraService(
        compraRepository,
        cosmeticoRepository,
        usuarioService,
        idsDefecto = listOf("cosmetico-default-1")
    )


    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
    val idsDefecto = listOf("cosmDef1", "cosmDef2")
    @Test
    fun `obtenerCosmeticosDelUsuario retorna cosmeticos comprados y por defecto`() {
        val idUsuario = "user-123"

        val comprasMock = listOf(
            Compra(idUsuario = idUsuario, idCosmetico = "cosm1"),
            Compra(idUsuario = idUsuario, idCosmetico = "cosm2")
        )

        val cosmeticosMock = listOf(
            Cosmetico(_id = "cosm1", nombre = "Cosmético 1", tipo = "tipo1", imagenUrl = "url1", precioMonedas = 10),
            Cosmetico(_id = "cosm2", nombre = "Cosmético 2", tipo = "tipo2", imagenUrl = "url2", precioMonedas = 20),
            Cosmetico(
                _id = "cosmDef1",
                nombre = "Cosmético Default 1",
                tipo = "tipo3",
                imagenUrl = "url3",
                precioMonedas = 0
            ),
            Cosmetico(
                _id = "cosmDef2",
                nombre = "Cosmético Default 2",
                tipo = "tipo4",
                imagenUrl = "url4",
                precioMonedas = 0
            )
        )

        every { compraRepository.findByIdUsuario(idUsuario) } returns comprasMock
        every { cosmeticoRepository.findAllById(any<List<String>>()) } answers {
            val ids = firstArg<List<String>>()
            cosmeticosMock.filter { it._id in ids }
        }

        // Si idsDefecto es propiedad de la clase, ajusta el mock o la instancia
        compraService.idsDefecto = idsDefecto  // si es mutable

        val resultado = compraService.obtenerCosmeticosDelUsuario(idUsuario)

        assertEquals(4, resultado.size)
        assertTrue(resultado.any { it._id == "cosm1" })
        assertTrue(resultado.any { it._id == "cosmDef1" })
    }

    @Test
    fun `registrarCompra exitoso`() {
        val compra = Compra(
            idUsuario = "user-123",
            idCosmetico = "cosmetico-1",
            fechaCompra = LocalDateTime.now()
        )
        val cosmetico = Cosmetico(
            _id = "cosmetico-1",
            tipo = "tipo",
            imagenUrl = "url",
            nombre = "Cosmetico 1",
            precioMonedas = 100
        )

        every { compraRepository.findByIdUsuarioAndIdCosmetico(any(), any()) } returns null

        every { cosmeticoRepository.findByIdOrNull(compra.idCosmetico) } returns cosmetico

        every { usuarioService.quitarMonedas(any(), any()) } just Runs

        every { compraRepository.save(any()) } answers { firstArg() }

        val response = compraService.registrarCompra(compra)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("Compra registrada correctamente", response.body)

        verify {
            usuarioService.quitarMonedas(compra.idUsuario, cosmetico.precioMonedas)
            compraRepository.save(any())
        }
    }

    @Test
    fun `registrarCompra falla si cosmético es defecto`() {
        val compra = Compra(
            idUsuario = "user-123",
            idCosmetico = "cosmetico-default-1",
            fechaCompra = LocalDateTime.now()
        )

        val exception = assertThrows<ConflictException> {
            compraService.registrarCompra(compra)
        }

        assertEquals("Conflicto (409). Este cosmético ya lo tienes por defecto", exception.message)
    }

    @Test
    fun `registrarCompra falla si ya comprado`() {
        val compra = Compra(
            idUsuario = "user-123",
            idCosmetico = "cosmetico-1",
            fechaCompra = LocalDateTime.now()
        )

        every { compraRepository.findByIdUsuarioAndIdCosmetico(compra.idUsuario, compra.idCosmetico) } returns compra

        val exception = assertThrows<ConflictException> {
            compraService.registrarCompra(compra)
        }

        assertEquals("Conflicto (409). Producto ya comprado", exception.message)
    }
}