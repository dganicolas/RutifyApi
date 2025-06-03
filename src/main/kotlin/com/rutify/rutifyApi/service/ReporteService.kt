package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Reporte
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.repository.IUsuarioRepository
import com.rutify.rutifyApi.repository.ReporteRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class ReporteService(private val reporteRepository: ReporteRepository, private val usuarioRepository: IUsuarioRepository) {

    fun reportarUsuario(idFirebase: String, authentication: Authentication): ResponseEntity<String> {
        val reportadorId = authentication.name // O extrae el UID si usas un principal custom

        val existe = reporteRepository.findByReportadorIdFirebaseAndReportadoIdFirebase(reportadorId, idFirebase)

        return if (existe != null) {
            throw ConflictException("Ya reportaste a este usuario.")
        } else {
            val nuevoReporte = Reporte(
                reportadorIdFirebase = reportadorId,
                reportadoIdFirebase = idFirebase
            )
            val usuario = usuarioRepository.findByIdFirebase(idFirebase) ?: throw NotFoundException("el ususario no existe")
            usuario.reportes++
            usuarioRepository.save(usuario)
            reporteRepository.save(nuevoReporte)
            ResponseEntity.status(HttpStatus.CREATED).body("Reporte enviado correctamente.")
        }
    }
}
