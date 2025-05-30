package com.rutify.rutifyApi.service

import com.google.firebase.auth.FirebaseAuth
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.repository.IUsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.http.HttpClient

@Service
class ServiceBase( val usuarioRepository: IUsuarioRepository) {

    @Autowired
    lateinit var httpClient: HttpClient
    fun obtenerUsuario(idFirebase: String) = usuarioRepository.findByIdFirebase(idFirebase) ?: throw NotFoundException("Usuario no encontrado")

    open fun getFirebaseAuthInstance(): FirebaseAuth = FirebaseAuth.getInstance()

    open fun createHttpClient(): HttpClient = HttpClient.newHttpClient()

}