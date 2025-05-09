package com.rutify.rutifyApi.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "Ejercicios")
data class Ejercicio(
    @Id
    val id: String,
    @Field
    val nombreEjercicio: String,
    @Field
    val descripcion: String,
    @Field
    val imagen: String,
    @Field
    val equipo: String,
    @Field
    val grupoMuscular: String,
    @Field
    val caloriasQuemadasPorRepeticion: Double,
    @Field
    val puntoGanadosPorRepeticion: Double
)