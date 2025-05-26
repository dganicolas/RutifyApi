package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.EstadisticasDiarias
import com.rutify.rutifyApi.dto.EstadisticasDto
import com.rutify.rutifyApi.dto.EstadisticasPatchDto
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.repository.IEstadisticasDiariasRepository
import com.rutify.rutifyApi.repository.IEstadisticasRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EstadisticasService {

    @Autowired
    private lateinit var estadisticasRepository: IEstadisticasRepository
    @Autowired
    private lateinit var estadisticasDiariasRepository: IEstadisticasDiariasRepository

    fun crearEstadisticas(estadisticas: Estadisticas, authentication: Authentication): ResponseEntity<EstadisticasDto> {

        // Comprobar si ya existen estadísticas para el usuario
        val existente = estadisticasRepository.findByIdFirebase(authentication.name)
        if (existente != null) {
            throw ConflictException("ya existe una estadisticas con el mismo id")
        }
        apuntarEstadisticasDiarias(estadisticas)
        val guardada = estadisticasRepository.save(estadisticas)
        return ResponseEntity.status(HttpStatus.CREATED).body(DTOMapper.estadisticasToEstadisticasDto(guardada))
    }

    private fun apuntarEstadisticasDiarias(estadisticas: Estadisticas) {
        val fechaHoy = LocalDate.now()

        val estadisticaExistente = estadisticasDiariasRepository
            .findByIdFirebaseAndFecha(estadisticas.idFirebase, fechaHoy)

        if (estadisticaExistente != null) {
            val actualizada = estadisticaExistente.copy(
                minActivo = estadisticaExistente.minActivo + estadisticas.minActivo,
                ejerciciosRealizados = estadisticaExistente.ejerciciosRealizados + estadisticas.ejerciciosRealizados,
                kCaloriasQuemadas = estadisticaExistente.kCaloriasQuemadas + estadisticas.kCaloriasQuemadas,
                PesoCorporal = estadisticas.pesoCorporal
            )
            estadisticasDiariasRepository.save(actualizada)
        } else {
            val nuevaEstadistica = EstadisticasDiarias(
                idFirebase = estadisticas.idFirebase,
                fecha = fechaHoy,
                minActivo = estadisticas.minActivo,
                PesoCorporal = estadisticas.pesoCorporal,
                ejerciciosRealizados = estadisticas.ejerciciosRealizados,
                kCaloriasQuemadas = estadisticas.kCaloriasQuemadas
            )
            estadisticasDiariasRepository.save(nuevaEstadistica)
        }
    }

    fun obtenerEstadisticasPorUsuarioId(usuarioId: String): ResponseEntity<EstadisticasDto> {
        val estadisticas = estadisticasRepository.findByIdFirebase(usuarioId)
            ?: throw NotFoundException("No se encontraron estadísticas para el usuario con ID: $usuarioId")

        return ResponseEntity.ok(DTOMapper.estadisticasToEstadisticasDto(estadisticas))
    }

    fun actualizarEstadisticas(usuarioId: String, estadisticasParciales: EstadisticasPatchDto, authentication: Authentication): ResponseEntity<EstadisticasDto> {
        if(usuarioId != authentication.name){
            throw UnauthorizedException("no tienes permiso para esa accion")
        }
        val existente = estadisticasRepository.findByIdFirebase(usuarioId)
            ?: throw NotFoundException("Estadísticas no encontradas")

        // Actualizar solo los campos que no son null en el DTO
        estadisticasParciales.lvlBrazo?.let { existente.lvlBrazo = it }
        estadisticasParciales.lvlAbdominal?.let { existente.lvlAbdominal = it }
        estadisticasParciales.lvlPecho?.let { existente.lvlPecho = it }
        estadisticasParciales.lvlEspalda?.let { existente.lvlEspalda = it }
        estadisticasParciales.lvlPiernas?.let { existente.lvlPiernas = it }
        estadisticasParciales.ejerciciosRealizados?.let { existente.ejerciciosRealizados = it }
        estadisticasParciales.kCaloriasQuemadas?.let { existente.kCaloriasQuemadas = it }
        apuntarEstadisticasDiarias(existente)
        val guardado = estadisticasRepository.save(existente)

        return ResponseEntity.ok(DTOMapper.estadisticasToEstadisticasDto(guardado))
    }

    fun reiniciarEstadisticas(usuarioId: String, authentication: Authentication): ResponseEntity<EstadisticasDto> {
        TODO("Not yet implemented")
    }

    fun eliminarEstadisticasPorUsuarioId(usuarioId: String, authentication: Authentication): ResponseEntity<EstadisticasDto> {
        TODO("Not yet implemented")
    }
}