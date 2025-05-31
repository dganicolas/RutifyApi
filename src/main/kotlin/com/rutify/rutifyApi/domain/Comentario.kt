package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate

@Document(collection = "Comentarios")
data class Comentario(
    @Id
    val _id:String? = null,
    @Field
    val idFirebase: String,
    @Field
    val nombreUsuario: String,
    @Field
    val avatarUrl: String,
    @Field
    val fechaPublicacion: LocalDate,
    @Field
    val estadoAnimo: String,
    @Field
    val texto: String,
    @Field
    val imagenUrl: String? = null,
    @Field
    var estado: Boolean? = null,
    @Field
    val idComentarioPadre: String? = null // Para respuestas
)
