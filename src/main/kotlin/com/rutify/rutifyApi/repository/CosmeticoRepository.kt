package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Cosmetico
import org.springframework.data.mongodb.repository.MongoRepository

interface CosmeticoRepository : MongoRepository<Cosmetico, String>
