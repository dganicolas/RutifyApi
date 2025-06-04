package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.Cosmetico
import com.rutify.rutifyApi.repository.CosmeticoRepository
import org.springframework.stereotype.Service

@Service
class CosmeticoService(private val cosmeticoRepository: CosmeticoRepository) {

    fun obtenerTodos(): List<Cosmetico> {
        return cosmeticoRepository.findAll()
    }
}