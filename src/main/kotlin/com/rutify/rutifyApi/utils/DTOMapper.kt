package com.rutify.rutifyApi.utils

import com.rutify.rutifyApi.domain.Estadisticas
import com.rutify.rutifyApi.domain.Usuario
import com.rutify.rutifyApi.dto.EstadisticasDto
import com.rutify.rutifyApi.dto.UsuarioregistradoDto

object DTOMapper {

    fun usuarioRegisterDTOToUsuarioProfileDto(usuario: Usuario) = UsuarioregistradoDto(
        nombre = usuario.nombre, correo = usuario.correo
    )

    fun estadisticasToEstadisticasDto(estadisticas: Estadisticas): EstadisticasDto {
        return EstadisticasDto(
            lvlBrazo = estadisticas.lvlBrazo,
            lvlPecho = estadisticas.lvlPecho,
            lvlEspalda = estadisticas.lvlEspalda,
            lvlPiernas = estadisticas.lvlPiernas,
            ejerciciosRealizados = estadisticas.ejerciciosRealizados,
            caloriasQuemadas = estadisticas.caloriasQuemadas
        )
    }
}