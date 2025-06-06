package com.rutify.rutifyApi.repository

import com.rutify.rutifyApi.domain.Reporte
import org.springframework.data.mongodb.repository.MongoRepository

interface ReporteRepository : MongoRepository<Reporte, String> {
    fun deleteAllByReportadorIdFirebase(idFirebase: String)
    fun deleteAllByReportadoIdFirebase(idFirebase: String)
    fun findByReportadorIdFirebaseAndReportadoIdFirebase(reportadorIdFirebase: String,
                                                         reportadoIdFirebase: String): Reporte?
}