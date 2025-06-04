package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.Compra
import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.service.CompraService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/compras")
class CompraController(
    private val compraService: CompraService
) {
    @GetMapping("/{idUsuario}")
    fun obtenerComprasUsuario(@PathVariable idUsuario: String): ResponseEntity<List<Cosmetico>> {
        val cosmeticos = compraService.obtenerCosmeticosDelUsuario(idUsuario)
        return ResponseEntity.ok(cosmeticos)
    }

    @PostMapping
    fun registrarCompra(@RequestBody compra: Compra): ResponseEntity<String> {
        return compraService.registrarCompra(compra)
    }
}