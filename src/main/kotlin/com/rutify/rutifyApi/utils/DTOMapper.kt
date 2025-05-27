package com.rutify.rutifyApi.utils

import com.rutify.rutifyApi.domain.*
import com.rutify.rutifyApi.dto.*

object DTOMapper {

    fun usuarioRegisterDTOToUsuarioProfileDto(usuario: Usuario) = UsuarioregistradoDto(
        nombre = usuario.nombre, correo = usuario.correo
    )

    fun estadisticasToEstadisticasDto(estadisticas: Estadisticas): EstadisticasDto {
        return EstadisticasDto(
            idFirebase = estadisticas.idFirebase,
            lvlBrazo = estadisticas.lvlBrazo,
            lvlPecho = estadisticas.lvlPecho,
            lvlEspalda = estadisticas.lvlEspalda,
            lvlAbdominal = estadisticas.lvlAbdominal,
            lvlPiernas = estadisticas.lvlPiernas,
            ejerciciosRealizados = estadisticas.ejerciciosRealizados,
            kCaloriasQuemadas = estadisticas.kCaloriasQuemadas
        )
    }

    fun rutinasDtoToRutina(dto: RutinaDTO, ejerciciosMap: Map<String, Int>): Rutina{
        return Rutina(
            nombre = dto.nombre,
            imagen = dto.imagen,
            descripcion = dto.descripcion,
            creadorId = dto.creadorId,
            equipo = dto.equipo,
            ejercicios = ejerciciosMap,
            esPremium = dto.esPremium
        )
    }

    fun ejercicioToEjercicioDto(ejercicio: Ejercicio,cantidad:Int):EjercicioDTO{
        return  EjercicioDTO(
            id = ejercicio.id!!,
            nombreEjercicio = ejercicio.nombreEjercicio,
            descripcion = ejercicio.descripcion,
            imagen = ejercicio.imagen,
            equipo = ejercicio.equipo,
            grupoMuscular = ejercicio.grupoMuscular,
            caloriasQuemadasPorRepeticion = ejercicio.caloriasQuemadasPorRepeticion,
            puntoGanadosPorRepeticion = ejercicio.puntoGanadosPorRepeticion,
            cantidad = cantidad
        )
    }
    fun votosDtoToVoto(voto:VotodDto): Voto {
        return Voto(
            id = voto.id,
            idFirebase = voto.idFirebase,
            idRutina = voto.idRutina,
            puntuacion = voto.puntuacion
        )
    }

    fun rutinaToRutinaBuscadorDto(rutina:Rutina): RutinaBuscadorDto {
        return RutinaBuscadorDto(
            id = rutina.id,
            nombre = rutina.nombre,
            imagen = rutina.imagen,
            descripcion = rutina.descripcion,
            cuantosEjercicios = rutina.ejercicios.size,
            esPremium = rutina.esPremium,
            votos = rutina.votos,
            totalVotos = rutina.totalVotos,
            equipo = rutina.equipo
        )
    }

    fun rutinaToRutinaDto(rutina:Rutina, ejercicioDto: MutableList<EjercicioDTO>): RutinaDTO {
        return RutinaDTO(
            id = rutina.id,
            nombre = rutina.nombre,
            imagen = rutina.imagen,
            descripcion = rutina.descripcion,
            creadorId = rutina.creadorId,
            ejercicios = ejercicioDto,
            equipo = rutina.equipo,
            esPremium = rutina.esPremium
        )
    }

    fun ejercicioDtoToEjercicio(ejercicioDTO: EjercicioDTO): Ejercicio {
        return Ejercicio(
            id = null,
            nombreEjercicio = ejercicioDTO.nombreEjercicio,
            descripcion = ejercicioDTO.descripcion,
            imagen = ejercicioDTO.imagen,
            equipo = ejercicioDTO.equipo,
            grupoMuscular = ejercicioDTO.grupoMuscular,
            caloriasQuemadasPorRepeticion = ejercicioDTO.caloriasQuemadasPorRepeticion,
            puntoGanadosPorRepeticion = ejercicioDTO.puntoGanadosPorRepeticion
        )
    }
    fun votoTovotosDto(voto:Voto): VotodDto {
        return VotodDto(
            id = voto.id,
            idFirebase = voto.idFirebase,
            idRutina = voto.idRutina,
            puntuacion = voto.puntuacion
        )
    }

    fun estadisticasDiariasToDto(estadisticas: EstadisticasDiarias): EstadisticasDiariasDto {
        return EstadisticasDiariasDto(
            _id = estadisticas._id,
            idFirebase = estadisticas.idFirebase,
            fecha = estadisticas.fecha,
            horasActivos = estadisticas.horasActivo,
            pesoCorporal = estadisticas.pesoCorporal,
            ejerciciosRealizados = estadisticas.ejerciciosRealizados,
            kCaloriasQuemadas = estadisticas.kCaloriasQuemadas
        )
    }

    fun listaEstadisticasDiariasToDto(lista: List<EstadisticasDiarias>): List<EstadisticasDiariasDto> {
        return lista.map { estadisticasDiariasToDto(it) }
    }
}