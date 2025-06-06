package com.rutify.rutifyApi.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.springframework.context.MessageSource
import java.util.*
import kotlin.test.Test

class MensajesServiceTest {

    private val messageSource = mockk<MessageSource>()
    private val mensajesService = MensajesService(messageSource)

    @Test
    fun `obtenerMensaje devuelve mensaje correctamente sin argumentos`() {
        val key = "mensaje.bienvenida"
        val expectedMessage = "Bienvenido"

        every { messageSource.getMessage(key, emptyArray(), Locale.getDefault()) } returns expectedMessage

        val resultado = mensajesService.obtenerMensaje(key)

        assertEquals(expectedMessage, resultado)
    }

    @Test
    fun `obtenerMensaje devuelve mensaje correctamente con argumentos`() {
        val key = "mensaje.saludo"
        val args = arrayOf("Juan", 5)
        val expectedMessage = "Hola Juan, tienes 5 mensajes nuevos"

        every { messageSource.getMessage(key, args, Locale.getDefault()) } returns expectedMessage

        val resultado = mensajesService.obtenerMensaje(key, *args)

        assertEquals(expectedMessage, resultado)
    }
}