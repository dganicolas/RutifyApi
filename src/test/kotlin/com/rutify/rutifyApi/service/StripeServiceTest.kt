package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.dto.PaymentRequestDto
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import com.stripe.param.PaymentIntentCreateParams
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class StripeServiceTest{

    private lateinit var usuariosService: UsuariosService
    private lateinit var stripeService: StripeService

    @BeforeEach
    fun setUp() {
        usuariosService = mockk()
        stripeService = StripeService(usuariosService)
        mockkStatic(Webhook::class)
        mockkStatic(PaymentIntent::class)  // Mockeamos método estático create()
    }

    @Test
    fun `crearPago caso bueno - crea pago correctamente`() {
        val request = PaymentRequestDto(userId = "user123", coins = 5000) // 5000 monedas = 50€

        val fakePaymentIntent = mockk<PaymentIntent> {
            every { clientSecret } returns "secret_123"
        }

        every { PaymentIntent.create(any<PaymentIntentCreateParams>()) } returns fakePaymentIntent

        val response = stripeService.crearPago(request)

        assertEquals("secret_123", response.clientSecret)

        verify(exactly = 1) { PaymentIntent.create(any<PaymentIntentCreateParams>()) }
    }

    @Test
    fun `crearPago caso malo - lanza error si monto menor a 50 céntimos`() {
        val request = PaymentRequestDto(userId = "user123", coins = 40) // 40 monedas = 0.40€

        val exception = assertThrows<IllegalArgumentException> {
            stripeService.crearPago(request)
        }

        assertEquals("La compra mínima es de 50 céntimos (50 monedas)", exception.message)

    }

    @Test
    fun `handleWebhook caso bueno - payload válido y firma correcta`() {
        val payload = """
            {
                "data": {
                    "object": {
                        "metadata": {
                            "userId": "user123",
                            "coins": 100
                        }
                    }
                }
            }
        """.trimIndent()
        val sigHeader = "valid_signature"
        val endpointSecret = "secret"

        every { Webhook.constructEvent(payload, sigHeader, endpointSecret) } returns mockk() // No importa qué devuelva

        every { usuariosService.anadirMonedas("user123", 100) } just Runs

        val response = stripeService.handleWebhook(payload, sigHeader, endpointSecret)

        assertEquals(200, response.statusCodeValue)
        assertEquals("", response.body)

        verify(exactly = 1) { usuariosService.anadirMonedas("user123", 100) }
    }

    @Test
    fun `handleWebhook caso malo - firma inválida`() {
        val payload = "{}"
        val sigHeader = "bad_signature"
        val endpointSecret = "secret"

        every { Webhook.constructEvent(payload, sigHeader, endpointSecret) } throws SignatureVerificationException("Firma inválida","")

        val response = stripeService.handleWebhook(payload, sigHeader, endpointSecret)

        assertEquals(400, response.statusCodeValue)
        assertEquals("Firma inválida", response.body)

        verify(exactly = 0) { usuariosService.anadirMonedas(any(), any()) }
    }

    @Test
    fun `handleWebhook caso malo - metadata incompleta`() {
        val payload = """
            {
                "data": {
                    "object": {
                        "metadata": {
                            "userId": "user123"
                        }
                    }
                }
            }
        """.trimIndent()
        val sigHeader = "valid_signature"
        val endpointSecret = "secret"

        every { Webhook.constructEvent(payload, sigHeader, endpointSecret) } returns mockk()

        val response = stripeService.handleWebhook(payload, sigHeader, endpointSecret)

        assertEquals(400, response.statusCodeValue)
        assertEquals("Metadata faltante o incompleta", response.body)

        verify(exactly = 0) { usuariosService.anadirMonedas(any(), any()) }
    }

    @Test
    fun `handleWebhook caso malo - excepción genérica`() {
        val payload = """
            {
                "data": {
                    "object": {
                        "metadata": {
                            "userId": "user123",
                            "coins": 100
                        }
                    }
                }
            }
        """.trimIndent()
        val sigHeader = "valid_signature"
        val endpointSecret = "secret"

        every { Webhook.constructEvent(payload, sigHeader, endpointSecret) } throws RuntimeException("Error inesperado")

        val response = stripeService.handleWebhook(payload, sigHeader, endpointSecret)

        assertEquals(500, response.statusCodeValue)
        assertTrue(response.body!!.contains("Error al procesar el webhook"))

        verify(exactly = 0) { usuariosService.anadirMonedas(any(), any()) }
    }
}