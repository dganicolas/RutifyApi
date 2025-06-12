package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.EstadisticasDiarias
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.EstadisticasDiariasDto
import com.rutify.rutifyApi.dto.EstadisticasDiariasPatchDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.IEstadisticasDiariasRepository
import com.rutify.rutifyApi.utils.DTOMapper.estadisticasDiariasToDto
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EstadisticasDiariasService(
    private val estadisticasDiariasRepository: IEstadisticasDiariasRepository
) {

    fun obtenerEstadisticasDiariasDia(idFirebase: String, fecha: LocalDate): ResponseEntity<EstadisticasDiariasDto?> {
        val resultado = estadisticasDiariasRepository.findByIdFirebaseAndFecha(idFirebase, fecha)
            ?: throw NotFoundException("Estadisticas diarias no existen")
        return ResponseEntity.ok(estadisticasDiariasToDto(resultado))
    }

    fun findByIdFirebaseAndFecha(idFirebase: String, fecha: LocalDate, patch: EstadisticasDiariasPatchDto): ResponseEntity<EstadisticasDiariasDto> {
        // Buscamos el registro existente para esa fecha
        val existente = estadisticasDiariasRepository.findByIdFirebaseAndFecha(idFirebase, fecha)

        // Si no existe, buscamos el último registro anterior para ese usuario
        val pesoAnterior = if (existente == null) {
            val ultimoAnterior = estadisticasDiariasRepository
                .findTopByIdFirebaseAndFechaBeforeOrderByFechaDesc(idFirebase, fecha)?.pesoCorporal ?: 0.0
            ultimoAnterior
        } else {
            existente.pesoCorporal
        }

        // Creamos o actualizamos la estadística diaria con peso tomado
        val actualizada = (existente ?: EstadisticasDiarias(
            _id = null,
            idFirebase = idFirebase,
            fecha = fecha,
            horasActivo = 0.0,
            kCaloriasQuemadas = 0.0,
            ejerciciosRealizados = 0,
            pesoCorporal = pesoAnterior
        )).copy(
            horasActivo = patch.horasActivo?.let { (existente?.horasActivo ?: 0.0) + it } ?: (existente?.horasActivo ?: 0.0),
            ejerciciosRealizados = patch.ejerciciosRealizados?.let { (existente?.ejerciciosRealizados ?: 0) + it } ?: (existente?.ejerciciosRealizados ?: 0),
            kCaloriasQuemadas = patch.kCaloriasQuemadas?.let { (existente?.kCaloriasQuemadas ?: 0.0) + it } ?: (existente?.kCaloriasQuemadas ?: 0.0),
            pesoCorporal = patch.pesoCorporal ?: pesoAnterior
        )

        val guardada = estadisticasDiariasRepository.save(actualizada)

        return ResponseEntity.ok(estadisticasDiariasToDto(guardada))
    }

    fun obtenerUltimos5Pesos(idFirebase: String): ResponseEntity<List<Double>> {
            val todas = estadisticasDiariasRepository.findTop5ByIdFirebase(idFirebase)

        val pesos = todas
            .map { it.pesoCorporal } // Más recientes primero

        // Rellenar con ceros al principio si hay menos de 5
        val resultado = pesos + List(5 - todas.size) { 0.0 }

        return ResponseEntity.ok(resultado)
    }

    fun eliminarEstadisticas(idFirebase: String, usuarioSolicitante: Usuario) {
        if (idFirebase != usuarioSolicitante.idFirebase &&usuarioSolicitante.rol != "admin") throw UnauthorizedException("No tienes permiso para aprobar comentarios")
        estadisticasDiariasRepository.deleteAllByIdFirebase(idFirebase)
    }

}
