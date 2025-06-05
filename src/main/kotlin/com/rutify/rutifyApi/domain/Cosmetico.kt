package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "cosmeticos")
data class Cosmetico(
    @Id val _id: String? = null,
    val nombre: String,
    val tipo: String,
    val precioMonedas: Int,
    val imagenUrl: String
)