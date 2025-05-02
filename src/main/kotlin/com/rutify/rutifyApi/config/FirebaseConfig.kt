package com.rutify.rutifyApi.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.http.HttpClient


@Configuration
class FirebaseConfig {
    @Autowired
    private lateinit var firebaseProperties: FirebaseProperties

    private fun initializeFirebaseApp(): FirebaseApp {
        val gson = Gson()
        val jsonObject = JsonObject().apply {
            addProperty("type", firebaseProperties.type)
            addProperty("project_id", firebaseProperties.projectId)
            addProperty("private_key_id", firebaseProperties.privateKeyId)
            addProperty("private_key", firebaseProperties.privateKey!!.replace("\\n", "\n"))
            addProperty("client_email", firebaseProperties.clientEmail)
            addProperty("client_id", firebaseProperties.clientId)
            addProperty("auth_uri", firebaseProperties.authUri)
            addProperty("token_uri", firebaseProperties.tokenUri)
            addProperty("auth_provider_x509_cert_url", firebaseProperties.authProviderX509CertUrl)
            addProperty("client_x509_cert_url", firebaseProperties.clientX509CertUrl)
            addProperty("universe_domain", firebaseProperties.universeDomain)
        }

        val serviceAccount = ByteArrayInputStream(gson.toJson(jsonObject).toByteArray())
        val credentials = GoogleCredentials.fromStream(serviceAccount)

        val options = FirebaseOptions.Builder()
            .setCredentials(credentials)
            .setProjectId(firebaseProperties.projectId)
            .build()

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    @Bean
    fun firestore(): Firestore {
        try {
            initializeFirebaseApp()
            return FirestoreClient.getFirestore()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize Firestore: ${e.message}", e)
        }
    }

    @Bean
    fun firebaseAuth(): FirebaseAuth {
        try {
            initializeFirebaseApp()
            return FirebaseAuth.getInstance()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize FirebaseAuth: ${e.message}", e)
        }
    }
}