package com.rutify.rutifyApi.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "Estadisticas")
data class Estadisticas(
    @Id
    val _id: ObjectId? = null,
    @Field
    val idFirebase: String,
    @Field
    val lvlBrazo: Float,
    @Field
    val lvlPecho: Float,
    @Field
    val lvlEspalda: Float,
    @Field
    val lvlPiernas: Float,
    @Field
    val ejerciciosRealizados: Int,
    @Field
    val caloriasQuemadas: Float
)