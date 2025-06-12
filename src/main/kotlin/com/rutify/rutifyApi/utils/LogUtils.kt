package com.rutify.rutifyApi.utils

import com.rutify.rutifyApi.domain.Log
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.io.path.notExists


object LogUtils {

    private val rutaLogs : String = "src/main/resources/logs"

    private fun createFichero(rutaFichero: Path) {
        // Si rutaFichero no existe, creo el sistema de archivos hasta el mismo
//        if (rutaFichero.notExists()) {
//            Files.createFile(rutaFichero)
//        }
    }
    private fun createDirectory(rutaDirectory: Path) {
        // Si rutaFichero no existe, creo el sistema de archivos hasta el mismo
//        if (rutaDirectory.notExists()) {
//            Files.createDirectories(rutaDirectory)
//        }
    }

    fun writeLog(log: Log) {

        val zonaHoraria = ZoneId.systemDefault()
        val fecha = Instant.now().atZone(zonaHoraria)

        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM")
        val monthName = monthFormat.format(cal.time)
        val dayFormat = SimpleDateFormat("EEEE")
        val dayName = dayFormat.format(cal.time)
        val diaNum = fecha.dayOfMonth

        val path = Path.of(rutaLogs).resolve(fecha.year.toString()).resolve(monthName.toString())

        createDirectory(path)

        val ficheroHoy = "$diaNum-$dayName"

        val pathFichero = Path.of(path.toString()).resolve(ficheroHoy)

        createFichero(pathFichero)

        val bw: BufferedWriter = Files.newBufferedWriter(pathFichero, StandardOpenOption.APPEND)

        bw.use {
            it.write(log.toString())
            it.newLine()
        }

    }
}