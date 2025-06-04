package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.domain.CoinPack
import com.rutify.rutifyApi.repository.CoinPackRepository
import org.springframework.stereotype.Service

@Service
class CoinPackService(private val repository: CoinPackRepository) {
    fun obtenerPacks(): List<CoinPack> = repository.findAll()
}
