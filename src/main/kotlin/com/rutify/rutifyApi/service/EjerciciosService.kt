package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Ejercicio
import com.rutify.rutifyApi.dto.EjercicioDTO
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEjercicioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class EjerciciosService {

    @Autowired
    private lateinit var ejerciciosRepository: IEjercicioRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun crearEjercicio(ejercicioDTO: EjercicioDTO): ResponseEntity<Ejercicio> {
        val error = validarEjercicio(ejercicioDTO)
        if (error != null) {
            throw ValidationException(error)
        }

        val ejercicio = Ejercicio(
            nombreEjercicio = ejercicioDTO.nombreEjercicio,
            descripcion = ejercicioDTO.descripcion,
            imagen = ejercicioDTO.imagen,
            equipo = ejercicioDTO.equipo,
            grupoMuscular = ejercicioDTO.grupoMuscular,
            caloriasQuemadasPorRepeticion = ejercicioDTO.caloriasQuemadasPorRepeticion,
            puntoGanadosPorRepeticion = ejercicioDTO.puntoGanadosPorRepeticion
        )

        val ejercicioGuardado = ejerciciosRepository.save(ejercicio)
        return ResponseEntity.ok(ejercicioGuardado)
    }

    private fun validarEjercicio(ejercicio: EjercicioDTO): String? {
        if (ejercicio.nombreEjercicio.isBlank()) return "El nombre del ejercicio no puede estar vacío"
        if (ejercicio.descripcion.isBlank()) return "La descripción no puede estar vacía"
        if (ejercicio.imagen.isBlank()) return "La URL de la imagen no puede estar vacía"
        if (!ejercicio.imagen.startsWith("http")) return "La URL de la imagen no es válida"
        if (ejercicio.grupoMuscular.isBlank()) return "El grupo muscular no puede estar vacío"
        if (ejercicio.caloriasQuemadasPorRepeticion <= 0) return "Las calorías por repetición deben ser mayores que 0"
        if (ejercicio.puntoGanadosPorRepeticion <= 0) return "Los puntos por repetición deben ser mayores que 0"
        return null
    }

    fun obtenerEjercicios(
            grupoMuscular: String?,
            equipo: String?,
            page: Int?,
            size: Int?
        ): List<Ejercicio> {
            // Si no hay filtros, devolver todos los ejercicios
            val filtroGrupoMuscular = grupoMuscular?.takeIf { it.isNotBlank() }
            val filtroEquipo = equipo?.takeIf { it.isNotBlank() }

            val query = Query()

            // Aplicar filtros si se pasan
            filtroGrupoMuscular?.let {
                query.addCriteria(Criteria.where("grupoMuscular").regex(it, "i"))
            }

            filtroEquipo?.let {
                query.addCriteria(Criteria.where("equipo").regex(it, "i"))
            }

            // Aplicar paginación si se pasan los parámetros
            page?.let {
                query.skip((it * (size ?: 10)).toLong())  // Calcular el offset (skip)
            }

            size?.let {
                query.limit(it)  // Limitar el tamaño de la página
            }

            // Consultar los ejercicios con la query preparada
            return mongoTemplate.find(query, Ejercicio::class.java)
        }
}