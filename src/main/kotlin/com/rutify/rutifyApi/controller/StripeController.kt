// src/main/kotlin/com/rutify/rutifyApi/controller/StripeController.kt
package com.rutify.rutifyApi.controller

import com.rutify.rutifyApi.domain.PaymentItem
import com.rutify.rutifyApi.dto.PaymentRequestDto
import com.rutify.rutifyApi.dto.PaymentResponse
import com.rutify.rutifyApi.service.StripeService
import com.stripe.Stripe
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/v1/pagos")
class StripeController(
    @Value("\${api_secret_stripe}") private val stripeSecretKey: String,
    @Value("\${stripe_webhook_secret}") private val endpointSecret: String,
    private val stripeService: StripeService
) {

    @PostConstruct
    fun init() {
        Stripe.apiKey = stripeSecretKey
    }

    @PostMapping
    fun createPaymentIntent(@RequestBody request: PaymentRequestDto): PaymentResponse {
       return stripeService.crearPago(request)
    }

    @PostMapping("/stripe-webhook")
    fun handleStripeWebhook(@RequestBody payload: String, @RequestHeader("Stripe-Signature") sigHeader: String): ResponseEntity<String> {
        return stripeService.handleWebhook(payload,sigHeader,endpointSecret)
    }


    private fun calculateOrderAmount(items: List<PaymentItem>): Int {
        return items.sumOf { it.amount.toInt() }
    }
}
