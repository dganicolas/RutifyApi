package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.dto.EstadisticasDiariasDto
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.repository.IEstadisticasDiariasRepository
import com.rutify.rutifyApi.utils.DTOMapper.estadisticasDiariasToDto
import com.rutify.rutifyApi.utils.DTOMapper.listaEstadisticasDiariasToDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class EstadisticasDiariasService {
    @Autowired
    private lateinit var estadisticasDiariasRepository: IEstadisticasDiariasRepository

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

}
