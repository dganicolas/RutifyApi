package com.rutify.rutifyApi.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rutify.rutifyApi.dto.PaymentRequestDto
import com.rutify.rutifyApi.dto.PaymentResponse
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class StripeService(
    private val usuariosService: UsuariosService,
) {
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
        return try {
            // Verifica la firma del evento
            Webhook.constructEvent(payload, sigHeader, endpointSecret)

            // Parseo manual del payload con Jackson
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val rootNode = mapper.readTree(payload)
            val metadata = rootNode
                ?.get("data")
                ?.get("object")
                ?.get("metadata")

            if (metadata == null || !metadata.has("userId") || !metadata.has("coins")) {
                return ResponseEntity.badRequest().body("❌ Metadata faltante o incompleta")
            }

            val userId = metadata["userId"].asText()
            val coins = metadata["coins"].asInt()

            println("✅ Recibido: $coins monedas para el usuario $userId")
            usuariosService.anadirMonedas(userId, coins)

            ResponseEntity.ok("")
        } catch (e: SignatureVerificationException) {
            ResponseEntity.status(400).body("Firma inválida")
        } catch (e: Exception) {
            ResponseEntity.status(500).body("Error al procesar el webhook: ${e.message}")
        }
    }

}
