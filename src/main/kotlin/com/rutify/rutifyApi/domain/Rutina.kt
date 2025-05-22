package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "Rutinas")
data class Rutina(
    @Id
    val id: String? = null,
    @Field
    val nombre: String,
    @Field
    val imagen:String,
    @Field
    val descripcion: String,
    @Field
    val creadorId: String,
    @Field
    val ejercicios: Map<String,Int>, // IDs de ejercicios asociados junto con sus repeticiones
    @Field
    val esPremium: Boolean,
    @Field
    var votos:Float = 0.0f,
    @Field
    var totalVotos: Int = 0,
    @Field
    val equipo:String = "no especificado",
    @Field
    val reportes: Int = 0,
)
