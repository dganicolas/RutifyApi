package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.bson.types.ObjectId

@Document(collection = "Usuarios")
data class Usuario(
    @Id
    var idFirebase: String,
    @Field
    var sexo: String,
    @Field
    var edad: Int,
    @Field
    var nombre: String,
    @Field
    var correo: String,
    @Field
    var gimnasioId: ObjectId? = null,
    @Field
    val avatar: String = "",
    @Field
    var esPremium: Boolean,
    @Field
    var perfilPublico: Boolean = true
)