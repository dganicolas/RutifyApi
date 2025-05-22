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
    var lvlBrazo: Double,
    @Field
    var lvlPecho: Double,
    @Field
    var lvlAbdominal: Double,
    @Field
    var lvlEspalda: Double,
    @Field
    var lvlPiernas: Double,
    @Field
    var ejerciciosRealizados: Int,
    @Field
    var kCaloriasQuemadas: Double
)