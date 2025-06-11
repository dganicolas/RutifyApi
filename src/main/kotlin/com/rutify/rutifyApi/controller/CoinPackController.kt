package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.CoinPack
import com.rutify.rutifyApi.service.CoinPackService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/coin-packs")
class CoinPackController(private val coinPackService: CoinPackService) {
    //documentado
    @GetMapping
    fun obtenerPacks(): List<CoinPack> = coinPackService.obtenerPacks()
}
