package com.rutify.rutifyApi.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "Reportes")
class Reporte (
    @Id
    val id: String? = null,
    @Field
    val reportadorIdFirebase:String,
    @Field
    val reportadoIdFirebase:String
)