package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "votos")
data class Voto(
    @Id
    val id: String? = null,
    @Field
    val idFirebase: String,
    @Field
    val idRutina: String,
    @Field
    var puntuacion: Float
)