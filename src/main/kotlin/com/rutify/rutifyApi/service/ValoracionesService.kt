package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Valoracion
import com.rutify.rutifyApi.domain.Voto
import com.rutify.rutifyApi.dto.VotoDto
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IValoracionesRepository
import com.rutify.rutifyApi.repository.IVotosRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ValoracionesService {

    @Autowired
    private lateinit var valoracionRepository: IValoracionesRepository

    @Autowired
    private lateinit var votoRepository: IVotosRepository

    @Autowired
    private lateinit var rutinaRepository: IRutinasRepository

    fun crearVoto(idFirebase: String, idRutina: String, puntuacion: Int): ResponseEntity<VotoDto> {

        validarVoto(idFirebase,idRutina,puntuacion)

        val voto = Voto(
            idFirebase = idFirebase,
            idRutina = idRutina,
            puntuacion = puntuacion
        )
        votoRepository.save(voto)
        val valoracion = crearOBuscarValoracion(idRutina)
        valoracion.puntuacionTotal += puntuacion

        valoracionRepository.save(valoracion)

        return ResponseEntity.ok(VotoDto(puntuacion = puntuacion))
    }

    fun obtenerValoraciones(idRutina: String): ResponseEntity<Valoracion> {
        val valoraciones = crearOBuscarValoracion(idRutina)
        return ResponseEntity.ok(valoraciones)
    }

    private fun validarVoto(idFirebase: String, idRutina: String, puntuacion: Int) {
        val rutinaExistente = rutinaRepository.findById(idRutina)
        if (rutinaExistente.isEmpty) {
            throw ValidationException("Esta rutina no existe")
        }
        val votoExistente = votoRepository.findByIdFirebaseAndIdRutina(idFirebase, idRutina)
        if (votoExistente != null) {
            throw ValidationException("Ya has votado esta rutina.")
        }
        if (puntuacion < 0 || puntuacion > 10) {
            throw ValidationException("La puntuación debe estar entre 0 y 10.")
        }

    }

    private fun crearOBuscarValoracion(idRutina: String): Valoracion {
        var valoracion = valoracionRepository.findByIdRutina(idRutina)
        if(valoracion == null){
            valoracion = Valoracion(idRutina = idRutina, puntuacionTotal = 0)
        }
        return valoracion

    }
}
