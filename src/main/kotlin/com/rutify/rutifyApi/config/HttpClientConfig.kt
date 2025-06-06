package com.rutify.rutifyApi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.http.HttpClient

@Configuration
class HttpClientConfig {

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient.newBuilder().build()
    }
}