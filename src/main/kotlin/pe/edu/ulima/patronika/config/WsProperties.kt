package pe.edu.ulima.patronika.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ws")
data class WsProperties(
    var url: String = "",
    var username: String = "",
    var password: String = "",
    var action: String = ""
)