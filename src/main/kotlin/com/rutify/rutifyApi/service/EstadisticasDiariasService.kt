package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.EstadisticasDiarias
import com.rutify.rutifyApi.dto.EstadisticasDiariasDto
import com.rutify.rutifyApi.dto.EstadisticasDiariasPatchDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.repository.IEstadisticasDiariasRepository
import com.rutify.rutifyApi.utils.DTOMapper.estadisticasDiariasToDto
import com.rutify.rutifyApi.utils.DTOMapper.listaEstadisticasDiariasToDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EstadisticasDiariasService(
    private val estadisticasDiariasRepository: IEstadisticasDiariasRepository
) {
    fun obtenerEstadisticasDiariasDeUnMes(idFirebase: String, fecha: LocalDate): ResponseEntity<List<EstadisticasDiariasDto>> {
        val fechaInicio = fecha.withDayOfMonth(1)
        val fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth())

        val resultados = estadisticasDiariasRepository
            .findByIdFirebaseAndFechaBetween(idFirebase, fechaInicio, fechaFin)

        return ResponseEntity.ok(listaEstadisticasDiariasToDto(resultados))
    }

    fun obtenerEstadisticasDiariasDia(idFirebase: String, fecha: LocalDate): ResponseEntity<EstadisticasDiariasDto?> {
        val resultado = estadisticasDiariasRepository.findByIdFirebaseAndFecha(idFirebase, fecha)
            ?: throw NotFoundException("Estadisticas diarias no existen")
        return ResponseEntity.ok(estadisticasDiariasToDto(resultado))
    }

    fun findByIdFirebaseAndFecha(idFirebase: String, fecha: LocalDate, patch: EstadisticasDiariasPatchDto): ResponseEntity<EstadisticasDiariasDto> {
        val existente = estadisticasDiariasRepository
            .findByIdFirebaseAndFecha(idFirebase, fecha)
            ?: EstadisticasDiarias(null,idFirebase,fecha,0.0,0.0,0,0.0)
        val actualizada = existente.copy(
            horasActivo = patch.horasActivo?.let { existente.horasActivo + it } ?: existente.horasActivo,
            ejerciciosRealizados = patch.ejerciciosRealizados?.let { existente.ejerciciosRealizados + it } ?: existente.ejerciciosRealizados,
            kCaloriasQuemadas = patch.kCaloriasQuemadas?.let { existente.kCaloriasQuemadas + it } ?: existente.kCaloriasQuemadas,
            pesoCorporal = patch.pesoCorporal ?: existente.pesoCorporal,
        )
        val guardada = estadisticasDiariasRepository.save(actualizada)

        return ResponseEntity.ok(estadisticasDiariasToDto(guardada))
    }

    fun obtenerUltimos5Pesos(idFirebase: String): ResponseEntity<List<Double>> {
            val todas = estadisticasDiariasRepository.findTop5ByIdFirebase(idFirebase)

        val pesos = todas
            .map { it.pesoCorporal }
            .take(5) // Más recientes primero

        // Rellenar con ceros al principio si hay menos de 5
        val resultado = List(5 - todas.size) { 0.0 } + pesos

        return ResponseEntity.ok(resultado.asReversed())
    }

}
