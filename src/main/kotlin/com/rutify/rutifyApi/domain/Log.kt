package com.rutify.rutifyApi.domain

import org.springframework.http.HttpStatusCode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Log (
    private val requestMethod: String,
    private val uri: String,
    private val estado: Boolean,
    private val statusCode: HttpStatusCode
) {

    private val fechaHora: Instant = Instant.now()
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    override fun toString(): String {
        return "${fechaHora.atZone(ZoneId.systemDefault()).format(formatter)},[${requestMethod.uppercase()}],$uri,${if (estado) "OK" else "FAILED"},$statusCode"
    }
}