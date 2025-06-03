package com.rutify.rutifyApi.service

import com.rutify.rutifyApi.dto.PaymentRequestDto
import com.rutify.rutifyApi.dto.PaymentResponse
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.http.ResponseEntity

class StripeService {
        fun crearPago(request: PaymentRequestDto): PaymentResponse {
            val eurPerCoin = 0.01 // 1 moneda = 0.01€
            val amountInCents = (request.coins * eurPerCoin * 100).toLong()

            if (amountInCents < 50) {
                throw IllegalArgumentException("La compra mínima es de 50 céntimos (50 monedas)")
            }

            val params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .putMetadata("userId", request.userId)
                .putMetadata("coins",request.coins.toString())
                .build()

            val intent = PaymentIntent.create(params)
            return PaymentResponse(intent.clientSecret)
        }

    fun handleWebhook(payload: String, sigHeader: String, endpointSecret: String): ResponseEntity<String> {
        try {
            val event = Webhook.constructEvent(
                payload,
                sigHeader,
                endpointSecret
            )

            if (event.type == "payment_intent.succeeded") {
                val intent = event.dataObjectDeserializer.`object`.get() as PaymentIntent

                // Extrae la cantidad pagada (opcional)
                val amount = intent.amount // en centavos
                val userId = intent.metadata["userId"] ?: return ResponseEntity.badRequest().body("No userId in metadata")

                val coins = (amount ?: 0) / 1 // En tu caso 1 moneda = 1 céntimo

                // Aquí haces la lógica para actualizar MongoDB y asignar las monedas al usuario
                // ejemplo: userRepository.addCoins(userId, coins)

                println("✅ Pago exitoso. Se añadieron $coins monedas al usuario $userId.")
            }

            return ResponseEntity.ok("Evento recibido")
        } catch (e: SignatureVerificationException) {
            throw ValidationException("Firma inválida")
        } catch (e: Exception) {
            return ResponseEntity.status(500).body("Error al procesar el evento")
        }
    }

}
