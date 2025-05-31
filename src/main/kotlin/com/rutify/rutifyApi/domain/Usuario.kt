package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.bson.types.ObjectId
import java.time.LocalDate

@Document(collection = "Usuarios")
data class Usuario(
    @Id
    var idFirebase: String,
    @Field
    var sexo: String,
    @Field
    var fechaNacimiento: LocalDate,
    @Field
    var nombre: String,
    @Field
    var correo: String,
    @Field
    var fechaUltimoReto: LocalDate,
    @Field
    var gimnasioId: ObjectId? = null,
    @Field
    var avatar: String = "",
    @Field
    var esPremium: Boolean,
    @Field
    var perfilPublico: Boolean = true,
    @Field
    var reportes: Int = 0,
    @Field
    var rol:String
)