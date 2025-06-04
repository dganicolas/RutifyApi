package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.service.CosmeticoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/cosmeticos")
class CosmeticoController(private val cosmeticoService: CosmeticoService) {

    @GetMapping
    fun obtenerTodos(): List<Cosmetico> {
        return cosmeticoService.obtenerTodos()
    }
}
