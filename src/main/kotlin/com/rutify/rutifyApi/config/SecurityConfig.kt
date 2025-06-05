package com.rutify.rutifyApi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig{

    @Bean
    fun configure(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }// Deshabilitar CSRF para APIs, si es necesario
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/v1/usuarios/registrarse").permitAll()  // URL pública
                    .requestMatchers("/v1/usuarios/acceder").permitAll()
                    .requestMatchers("/v1/pagos/stripe-webhook").permitAll()
                    .requestMatchers("/terminos-y-condiciones.html").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt { } }
            .build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val issuer = "https://securetoken.google.com/gymquest-a2c3d"

        val decoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com")
            .build()

        decoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer(issuer)
        )

        return decoder
    }
}

