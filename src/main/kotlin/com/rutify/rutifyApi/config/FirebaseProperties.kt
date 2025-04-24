package com.rutify.rutifyApi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "firebase")
class FirebaseProperties {
    var type: String? = null
    var projectId: String? = null
    var privateKeyId: String? = null
    var privateKey: String? = null
    var clientEmail: String? = null
    var clientId: String? = null
    var authUri: String? = null
    var tokenUri: String? = null
    var authProviderX509CertUrl: String? = null
    var clientX509CertUrl: String? = null
    var universeDomain: String? = null
}