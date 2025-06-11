package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.service.ReporteService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/reportes")
class ReportesController(private val reporteService: ReporteService) {

    //documentado 
    @PostMapping("/reportar/{idFirebase}")
    fun reportarUsuario(@PathVariable idFirebase: String,authentication: Authentication): ResponseEntity<String> {
        return reporteService.reportarUsuario(idFirebase,authentication)
    }
}
