package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Ejercicio
import com.rutify.rutifyApi.dto.EjercicioDTO
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEjercicioRepository
import com.rutify.rutifyApi.utils.DTOMapper.ejercicioDtoToEjercicio
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
        validarEjercicio(ejercicioDTO)
        val ejercicioGuardado = ejerciciosRepository.save(ejercicioDtoToEjercicio(ejercicioDTO))
        return ResponseEntity.ok(ejercicioGuardado)
    }

    private fun validarEjercicio(ejercicio: EjercicioDTO) {
        if (ejercicio.nombreEjercicio.isBlank()) throw ValidationException("El nombre del ejercicio no puede estar vacío")
        if (ejercicio.descripcion.isBlank()) throw ValidationException("La descripción no puede estar vacía")
        if (ejercicio.imagen.isBlank()) throw ValidationException("La URL de la imagen no puede estar vacía")
        if (!ejercicio.imagen.startsWith("http")) throw ValidationException("La URL de la imagen no es válida")
        if (ejercicio.grupoMuscular.isBlank()) throw ValidationException("El grupo muscular no puede estar vacío")
        if (ejercicio.caloriasQuemadasPorRepeticion <= 0) throw ValidationException("Las calorías por repetición deben ser mayores que 0")
        if (ejercicio.puntoGanadosPorRepeticion <= 0) throw ValidationException("Los puntos por repetición deben ser mayores que 0")
    }

    fun obtenerEjercicios(
        grupoMuscular: String?,
        equipo: String?,
        page: Int?,
        size: Int?,
    ): List<EjercicioDTO> {
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
        val ejercicios = mongoTemplate.find(query, Ejercicio::class.java)

        // Consultar los ejercicios con la query preparada
        return ejercicios.map { ejercicio ->
            // Aquí conviertes de entidad `Ejercicio` a DTO si lo necesitas
            EjercicioDTO(
                id = ejercicio.id!!,
                nombreEjercicio = ejercicio.nombreEjercicio,
                descripcion = ejercicio.descripcion,
                equipo = ejercicio.equipo,
                grupoMuscular = ejercicio.grupoMuscular,
                imagen = ejercicio.imagen,
                caloriasQuemadasPorRepeticion = ejercicio.caloriasQuemadasPorRepeticion,
                puntoGanadosPorRepeticion = ejercicio.puntoGanadosPorRepeticion,
                cantidad = 0
            )
        }
    }
}