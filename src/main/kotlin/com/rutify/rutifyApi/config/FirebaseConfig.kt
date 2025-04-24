package com.rutify.rutifyApi.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.InputStream


@Configuration
class FirebaseConfig {
    @Autowired
    private lateinit var firebaseProperties: FirebaseProperties

    @Bean
    fun firestore(): Firestore {
        try {
            val gson: Gson = Gson()
            val jsonObject: JsonObject = JsonObject()

            jsonObject.addProperty("type", firebaseProperties.type)
            jsonObject.addProperty("project_id", firebaseProperties.projectId)
            jsonObject.addProperty("private_key_id", firebaseProperties.privateKeyId)
            jsonObject.addProperty(
                "private_key", firebaseProperties.privateKey!!.replace("\\n", "\n"))
            jsonObject.addProperty("client_email", firebaseProperties.clientEmail)
            jsonObject.addProperty("client_id", firebaseProperties.clientId)
            jsonObject.addProperty("auth_uri", firebaseProperties.authUri)
            jsonObject.addProperty("token_uri", firebaseProperties.tokenUri)
            jsonObject.addProperty("auth_provider_x509_cert_url", firebaseProperties.authProviderX509CertUrl)
            jsonObject.addProperty("client_x509_cert_url", firebaseProperties.clientX509CertUrl)
            jsonObject.addProperty("universe_domain", firebaseProperties.universeDomain)

            val serviceAccount: InputStream = ByteArrayInputStream(gson.toJson(jsonObject).toByteArray())

            val credentials = GoogleCredentials.fromStream(serviceAccount)

            val options = FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId(firebaseProperties.projectId)
                .build()

            // Inicializar Firebase
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }

            return FirestoreClient.getFirestore()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize Firestore: " + e.message, e)
        }
    }
}