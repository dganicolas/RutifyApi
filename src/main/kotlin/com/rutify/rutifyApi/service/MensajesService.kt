package com.rutify.rutifyApi.service

import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class MensajesService(private val messageSource: MessageSource) {

    fun obtenerMensaje(key: String, vararg args: Any): String {
        return messageSource.getMessage(key, args, Locale.getDefault())
    }
}
