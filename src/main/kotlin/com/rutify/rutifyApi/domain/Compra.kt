package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "compras")
data class Compra(
    @Id val id: String? = null,
    val idUsuario: String,
    val idCosmetico: String,
    val fechaCompra: LocalDateTime = LocalDateTime.now()
)