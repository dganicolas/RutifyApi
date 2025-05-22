package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.dto.VotodDto
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.repository.IVotosRepository
import com.rutify.rutifyApi.utils.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class VotosService {

    @Autowired
    private lateinit var rutinaRepository: IRutinasRepository
    @Autowired
    private lateinit var votosRepository: IVotosRepository
    @Autowired
    private lateinit var usuarioRepository: IUsuarioRepository

    private fun validarVotos(voto: VotodDto){
        if(usuarioRepository.findByIdFirebase(voto.idFirebase) == null) throw ValidationException("el usuario no existe")
        if(rutinaRepository.findById(voto.idRutina).isEmpty) throw ValidationException("la rutina no existe")

        if (voto.puntuacion <= 0.0 || voto.puntuacion >= 5.0) throw ValidationException("La puntuación debe ser mayor a 0.0 y menor a 5.0")

    }

    private fun anotarVoto(puntuacion:Float,votante:Int,idRutina:String){
        val rutina = rutinaRepository.findById(idRutina).get()
        rutina.totalVotos += votante
        rutina.votos += puntuacion
        rutinaRepository.save(rutina)
    }

    fun agregarVotacion(voto: VotodDto, authentication: Authentication): ResponseEntity<VotodDto> {
        if (voto.idFirebase != authentication.name) {
            throw UnauthorizedException("No tienes permiso para crear este voto a otro usuario")
        }
        validarVotos(voto)
        if(votosRepository.findByIdFirebaseAndIdRutina(voto.idFirebase,voto.idRutina) != null){
            throw ConflictException("el voto ya existe")
        }
        anotarVoto(voto.puntuacion,1,voto.idRutina)
        val votoGuardado = votosRepository.save(DTOMapper.votosDtoToVoto(voto))
        return ResponseEntity.status(HttpStatus.CREATED).body(DTOMapper.votoTovotosDto(votoGuardado))
    }

    fun actualizarVotos(voto: VotodDto, authentication: Authentication): ResponseEntity<VotodDto> {
        if (voto.idFirebase != authentication.name) {
            throw UnauthorizedException("No tienes permiso para actualizar este voto")
        }
        validarVotos(voto)
        val votoExistente = votosRepository.findById(voto.id!!).get()
        val diferencia = voto.puntuacion - votoExistente.puntuacion
        votosRepository.save(DTOMapper.votosDtoToVoto(voto))
        anotarVoto(diferencia,0,voto.idRutina)
        return ResponseEntity.ok(voto)
    }

    fun eliminarVoto(voto: VotodDto, authentication: Authentication): ResponseEntity<Void> {
        val votoExistente = votosRepository.findByIdFirebaseAndIdRutina(voto.idFirebase, voto.idRutina)
            ?: throw ValidationException("el voto no existe")
        if (voto.idFirebase != authentication.name) {
            throw UnauthorizedException("No tienes permiso para eliminar este voto")
        }
        anotarVoto(-votoExistente.puntuacion,-1,votoExistente.idRutina)
        votosRepository.delete(votoExistente)
        return ResponseEntity.noContent().build()
    }

    fun obtenerVoto(idFirebase: String, idRutina: String, authentication: Authentication): ResponseEntity<VotodDto> {
        if (idFirebase != authentication.name) {
            throw UnauthorizedException("No tienes permiso para ver este voto")
        }
        val votoExistente = votosRepository.findByIdFirebaseAndIdRutina(idFirebase, idRutina)
            ?: throw ValidationException("el voto no existe")

        return ResponseEntity.ok(DTOMapper.votoTovotosDto(votoExistente))
    }


}
