package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Rutina
import com.rutify.rutifyApi.dto.EjercicioDTO
import com.rutify.rutifyApi.dto.RutinaBuscadorDto
import com.rutify.rutifyApi.dto.RutinaDTO
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.repository.IEjercicioRepository
import com.rutify.rutifyApi.repository.IRutinasRepository
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.utils.DTOMapper.ejercicioToEjercicioDto
import com.rutify.rutifyApi.utils.DTOMapper.rutinaToRutinaBuscadorDto
import com.rutify.rutifyApi.utils.DTOMapper.rutinaToRutinaDto
import com.rutify.rutifyApi.utils.DTOMapper.rutinasDtoToRutina
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class RutinaService(
    private val rutinaRepository: IRutinasRepository,
    private val ejercicioRepository: IEjercicioRepository,
    usuariosRepository: IUsuarioRepository,
    private val emailService: EmailService,
    private val mongoTemplate: MongoTemplate
):ServiceBase(usuariosRepository) {



    fun crearRutina(dto: RutinaDTO): ResponseEntity<RutinaDTO> {
        validarRutina(dto)

        val ejerciciosMap: Map<String, Int> = dto.ejercicios.associate { ejercicioDto ->
            obtenerIdDeEjercicio(ejercicioDto.id, ejercicioDto.nombreEjercicio) to ejercicioDto.cantidad
        }

        rutinaRepository.save(rutinasDtoToRutina(dto, ejerciciosMap))
        return ResponseEntity.status(HttpStatus.CREATED).body(dto)
    }

    private fun obtenerIdDeEjercicio(id: String, nombreEjercicio: String): String {
        return ejercicioRepository.findById(id)
            .orElseThrow { throw NotFoundException("El ejercicio $nombreEjercicio no existe") }.id
            ?: throw ValidationException("El ejercicio no tiene ID")
    }

    private fun validarRutina(dto: RutinaDTO) {
        if (dto.nombre.isBlank()) throw ValidationException("El nombre no puede estar vacío")
        if (dto.descripcion.isBlank()) throw ValidationException("La descripción no puede estar vacía")
        if (dto.ejercicios.isEmpty()) throw ValidationException("La rutina debe tener al menos un ejercicio")
        if (dto.creadorId.isBlank()) throw ValidationException("El ID del creador no puede estar vacío")
    }

    fun obtenerRutinasBuscador(page: Int, size: Int, equipo: String?): ResponseEntity<List<RutinaBuscadorDto>> {
        val query = crearQueryPersonalizada(equipo, page, size)
        val rutinas = mongoTemplate.find(query, Rutina::class.java)

        return ResponseEntity.ok(rutinas.map { rutinaToRutinaBuscadorDto(it) })
    }

    private fun crearQueryPersonalizada(equipo: String?, page: Int, size: Int): Query {
        val query = Query()

        equipo?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("equipo").regex(it, "i"))
        }

        query.with(Sort.by(Sort.Direction.DESC, "id")).skip((page * size).toLong()).limit(size)
        return query
    }


    fun obtenerRutinasPorAutor(creadorId: String): ResponseEntity<List<RutinaBuscadorDto>> {
        if (creadorId.isBlank()) {
            throw ValidationException("El ID del creador no puede estar vacío")
        }

        val rutinas = rutinaRepository.findAllByCreadorId(creadorId)
        val resultado = rutinas.map { rutinaToRutinaBuscadorDto(it) }

        return ResponseEntity.ok(resultado)
    }

    fun obtenerRutinaPorId(idRutina: String): ResponseEntity<RutinaDTO> {
        val rutina = rutinaRepository.findById(idRutina)
            .orElseThrow { NotFoundException("No se encontró la rutina con ID: $idRutina") }

        val ejercicioDto: MutableList<EjercicioDTO> = mutableListOf()

        ejercicioRepository.findAllById(rutina.ejercicios.keys.toList()).forEachIndexed { index, ejercicio ->
            ejercicioDto.add(
                ejercicioToEjercicioDto(ejercicio, rutina.ejercicios.values.toList()[index])
            )
        }

        return ResponseEntity.ok(rutinaToRutinaDto(rutina, ejercicioDto))

    }

    fun eliminarRutina(idRutina: String, authentication: Authentication): ResponseEntity<Void> {

        val usuario = usuarioRepository.findByIdFirebase(authentication.name) ?: throw NotFoundException("El usuario ${authentication.name} no existe ")
        val rutina = rutinaRepository.findById(idRutina).orElseThrow { NotFoundException("No se encontró la rutina con ID: $idRutina") }!!

        if (rutina.creadorId != authentication.name && usuario.rol != "admin") throw UnauthorizedException("No tienes permiso para crear este voto a otro usuario")
        if( usuario.rol == "admin")emailService.enviarCorreoNotificacion(usuario.correo,"rutina eliminada", "Hemos eliminado tu rutina ${rutina.nombre} por incumplimiento de la comunidad")

        rutinaRepository.delete(rutina)
        return ResponseEntity.noContent().build()
    }

    fun buscarRutinas(nombre: String?): ResponseEntity<List<RutinaBuscadorDto>> {
        val criteriaList = mutableListOf<Criteria>()

        nombre?.takeIf { it.isNotBlank() }?.let {
            // "^" indica que debe comenzar con ese texto
            criteriaList.add(Criteria.where("nombre").regex("^$it", "i"))
        }
        val query = Query()
        if (criteriaList.isNotEmpty()) {
            query.addCriteria(Criteria().andOperator(*criteriaList.toTypedArray()))
        }

        val rutinas = mongoTemplate.find(query, Rutina::class.java)
        val resultado = rutinas.map { rutinaToRutinaBuscadorDto(it) }

        return ResponseEntity.ok(resultado)
    }
}
