package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Compra
import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.exception.exceptions.ConflictException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.repository.CompraRepository
import com.rutify.rutifyApi.repository.CosmeticoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CompraService(
    private val compraRepository: CompraRepository,
    private val cosmeticoRepository: CosmeticoRepository,
    private val usuarioService: UsuariosService
) {
    private val idsDefecto = listOf(
        "684083a658387bec10e36087",
        "6840839e58387bec10e36086",
        "6840838c58387bec10e36085",
        "684078ca58387bec10e36083"
    )

    fun obtenerCosmeticosDelUsuario(idUsuario: String): List<Cosmetico> {
        val compras = compraRepository.findByIdUsuario(idUsuario)
        val idsComprados = compras.map { it.idCosmetico }
        val idsTotales = idsComprados + idsDefecto
        return cosmeticoRepository.findAllById(idsTotales)
    }

    fun registrarCompra(compra: Compra): ResponseEntity<String> {
        if (idsDefecto.contains(compra.idCosmetico)) {
            throw ConflictException("Este cosmético ya lo tienes por defecto")
        }
        val yaComprado = compraRepository.findByIdUsuarioAndIdCosmetico(
            compra.idUsuario,
            compra.idCosmetico
        ) != null

        return if (yaComprado) {
            throw ConflictException("Producto ya comprado")
        } else {
            val comesticoAComprar = cosmeticoRepository.findByIdOrNull(compra.idCosmetico) ?: throw NotFoundException("Cosmético no encontrado")
            usuarioService.quitarMonedas(compra.idUsuario,comesticoAComprar.precioMonedas)
            val nuevaCompra = compra.copy(fechaCompra = LocalDateTime.now())
            compraRepository.save(nuevaCompra)
            ResponseEntity.status(HttpStatus.CREATED).body("Compra registrada correctamente")
        }
    }
}
