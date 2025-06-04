package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.CoinPack
import org.springframework.data.mongodb.repository.MongoRepository

interface CoinPackRepository : MongoRepository<CoinPack, String>
