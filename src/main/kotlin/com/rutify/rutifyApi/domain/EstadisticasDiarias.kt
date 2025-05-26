package com.rutify.rutifyApi.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate

@Document(collection = "EstadisticasDiarias")
data class EstadisticasDiarias(
    @Id
    val _id: String? = null,
    @Field
    val idFirebase: String,
    @Field
    val fecha: LocalDate,
    @Field
    val minActivo: Double,
    @Field
    val PesoCorporal: Double,
    @Field
    var ejerciciosRealizados: Int,
    @Field
    var kCaloriasQuemadas: Double
)