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
    val descripcion: String,
    @Field
    val creadorId: String,
    @Field
    val ejercicios: List<Ejercicio>, // IDs de ejercicios asociados
    @Field
    val esPremium: Boolean,
    @Field
    val equipo:String = "no especificado",
    @Field
    val reportes: Int = 0,
)
