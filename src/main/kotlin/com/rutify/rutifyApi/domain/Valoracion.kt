package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "valoraciones")
data class Valoracion(
    @Id
    val id: String? = null,
    @Field
    val idRutina: String,    // ID de la rutina valorada
    @Field
    var puntuacionTotal: Int // Puntuación total acumulada
)