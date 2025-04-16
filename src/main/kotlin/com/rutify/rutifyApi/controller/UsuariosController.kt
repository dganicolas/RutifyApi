package com.rutify.rutifyApi.controller

import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/usuarios")
class UsuariosController {

    private val db = FirestoreClient.getFirestore();

    // Endpoint para crear un nuevo usuario
    @PostMapping("/create")
    fun createUser(@RequestBody user: User): ResponseEntity<String> {
        return try {
            val userRef = db.collection("Usuarios").document() // genera ID automático
            val newUserId = userRef.id
            val userWithId = user.copy(id = newUserId) // si quieres guardar el ID dentro del usuario
            userRef.set(userWithId).get()
            ResponseEntity("Usuario registrado correctamente", HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity("Error creando el usuario: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    // Endpoint para obtener un usuario
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<User> {
        return try {
            val userRef = db.collection("Usuarios").document(id)
            val document = userRef.get().get() // Esperamos a la respuesta (bloqueante)

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                ResponseEntity.ok(user)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el usuario: ${e.message}")
        }
    }
}

data class User(val id: String?, val name: String, val email: String)