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
    var rol:String,
    @Field
    var monedas:Int = 0,
    @Field
    var indumentaria: Indumentaria = Indumentaria(
        colorPiel = "https://i.ibb.co/mkfD3hj/brazos-1.webp",
        camiseta = "https://i.ibb.co/ccLWvVh3/camisetaavatar-2.webp",
        pantalon = "https://i.postimg.cc/pdyyJWQ0/pantalonavatar.webp",
        tenis = "https://i.ibb.co/Y7KVHNKC/zapatoavatar.webp"
    )
)