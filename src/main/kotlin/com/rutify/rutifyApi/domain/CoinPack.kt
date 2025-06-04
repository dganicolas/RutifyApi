package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "coin_packs")
data class CoinPack(
    @Id
    val id: String? = null,
    val nombre: String,
    val monedas: Int,
    val precio: Double,
    val imageUrl: String? = null
)