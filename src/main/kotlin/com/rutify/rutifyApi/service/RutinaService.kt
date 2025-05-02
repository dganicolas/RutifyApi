package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Rutina
import com.rutify.rutifyApi.dto.RutinaBuscadorDto
import com.rutify.rutifyApi.dto.RutinaDTO
import com.rutify.rutifyApi.dto.RutinaPaginadaResponseDto
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IRutinasRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class RutinaService {

    @Autowired
    private lateinit var rutinaRepository: IRutinasRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun crearRutina(dto: RutinaDTO): ResponseEntity<Rutina> {
        val error = validarRutina(dto)
        if (error != null) throw ValidationException(error)

        val rutina = Rutina(
            nombre = dto.nombre,
            descripcion = dto.descripcion,
            creadorId = dto.creadorId,
            ejercicios = dto.ejercicios,
            esPremium = dto.esPremium
        )

        val guardada = rutinaRepository.save(rutina)
        return ResponseEntity.ok(guardada)
    }

    private fun validarRutina(dto: RutinaDTO): String? {
        if (dto.nombre.isBlank()) return "El nombre no puede estar vacío"
        if (dto.descripcion.isBlank()) return "La descripción no puede estar vacía"
        if (dto.ejercicios.isEmpty()) return "La rutina debe tener al menos un ejercicio"
        if (dto.creadorId.isBlank()) return "El ID del creador no puede estar vacío"
        return null
    }

    fun obtenerRutinasBuscador(page: Int, size: Int, equipo: String?): ResponseEntity<RutinaPaginadaResponseDto> {

        val query = crearQueryPersonalizada(equipo,page,size)
        val rutinas = mongoTemplate.find(query, Rutina::class.java)
        val totalItems = mongoTemplate.count(query, Rutina::class.java)

        val dtoList = rutinas.map {
            RutinaBuscadorDto(
                id = it.id,
                nombre = it.nombre,
                descripcion = it.descripcion,
                cuantosEjercicios = it.ejercicios.size,
                esPremium = it.esPremium,
                equipo = it.equipo
            )
        }

        val totalPages = if (size == 0) 1 else Math.ceil(totalItems.toDouble() / size).toInt()

        val response = RutinaPaginadaResponseDto(
            rutinas = dtoList,
            totalItems = totalItems,
            totalPages = totalPages,
            currentPage = page
        )

        return ResponseEntity.ok(response)
    }

    private fun crearQueryPersonalizada(equipo: String?, page: Int, size: Int): Query {
        val query = Query()

        equipo?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("equipo").regex(it, "i"))
        }

        query.with(Sort.by(Sort.Direction.DESC, "id"))
        query.skip((page * size).toLong())
        query.limit(size)
        return query
    }
}
