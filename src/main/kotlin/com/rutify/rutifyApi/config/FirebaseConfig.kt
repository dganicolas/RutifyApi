package com.rutify.rutifyApi.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException

@Configuration
class FirebaseConfig {
    @Bean
    @Throws(IOException::class)
    fun initialize() {
        val firebaseServiceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        val serviceAccount = ByteArrayInputStream(firebaseServiceAccountJson.toByteArray())

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }
}