package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Ejercicio
import org.springframework.data.mongodb.repository.MongoRepository

interface IEjercicioRepository : MongoRepository<Ejercicio, String>