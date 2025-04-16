package com.rutify.rutifyApi.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.auth.UserRecord
import org.springframework.stereotype.Service


@Service
class AuthService {
    fun registerUser(email: String?, password: String?): String {
        try {
            val request = UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)

            val userRecord = FirebaseAuth.getInstance().createUser(request)
            return userRecord.uid // Retornamos el UID del usuario
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error registrando usuario: " + e.message
        }
    }

    fun verifyIdToken(idToken: String?): FirebaseToken? {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null // En producción, sería mejor lanzar una excepción
        }
    }


}