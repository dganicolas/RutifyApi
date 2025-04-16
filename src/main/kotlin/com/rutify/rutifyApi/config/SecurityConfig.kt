package com.rutify.rutifyApi.config

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Configuration
@EnableWebSecurity
class SecurityConfig{

    fun configure(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }// Deshabilitar CSRF para APIs, si es necesario
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/v1/usuarios/publico").permitAll()  // URL pública
                    .requestMatchers("/v1/usuarios/**").authenticated()  // URLs privadas para usuarios autenticados
                    .anyRequest().authenticated() // Aseguramos que todas las solicitudes requieran autenticación
            }
            .addFilterBefore(FirebaseAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}

class FirebaseAuthenticationFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = request.getHeader("Authorization")?.substring(7) // Asumiendo "Bearer <token>"

        if (token != null && token.isNotEmpty()) {
            try {
                val firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token)
                val authentication = UsernamePasswordAuthenticationToken(firebaseToken.uid, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: FirebaseAuthException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido")
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}
