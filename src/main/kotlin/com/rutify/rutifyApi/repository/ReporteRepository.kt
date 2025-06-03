package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Reporte
import org.springframework.data.mongodb.repository.MongoRepository

interface ReporteRepository : MongoRepository<Reporte, String> {
    fun findByReportadorIdFirebaseAndReportadoIdFirebase(reportadorIdFirebase: String,
                                                         reportadoIdFirebase: String): Reporte?
}