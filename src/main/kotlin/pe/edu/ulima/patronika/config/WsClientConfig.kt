package pe.edu.ulima.patronika.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.nio.charset.StandardCharsets

@Configuration
@EnableConfigurationProperties(WsProperties::class)
class WsClientConfig(
    private val ws: WsProperties
) {
    private val log = LoggerFactory.getLogger(WsClientConfig::class.java)

    @Bean("webClient")
    fun webClient(): WebClient {
        val httpClient = HttpClient.create()
            .followRedirect(true)

        val basicAuthFilter = ExchangeFilterFunction.ofRequestProcessor { request ->
            val withAuth = ClientRequest.from(request)
                .headers { headers ->
                    headers.setBasicAuth(ws.username, ws.password, StandardCharsets.ISO_8859_1)
                }
                .build()
            Mono.just(withAuth)
        }

        val authPresenceFilter = ExchangeFilterFunction.ofRequestProcessor { request ->
            val headers = request.headers()
            val authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION)
            val hasAuth = authHeader != null
            val authScheme = authHeader?.substringBefore(" ")
            val authLen = authHeader?.length

            val headersForLog = linkedMapOf<String, List<String>>()
            headers.forEach { k: String, v: List<String> ->
                headersForLog[k] =
                    if (k.equals(HttpHeaders.AUTHORIZATION, ignoreCase = true)) {
                        listOf("<redacted>")
                    } else {
                        v
                    }
            }

            log.debug(
                "SOAP request {} {} authorizationHeaderPresent={} authorizationScheme={} authorizationHeaderLen={} authorizationHeader={} headers={}",
                request.method(),
                request.url(),
                hasAuth,
                authScheme,
                authLen,
                authHeader,
                headersForLog
            )
            Mono.just(request)
        }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(ws.url)
            .filter(basicAuthFilter)
            .filter(authPresenceFilter)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=UTF-8")
            .defaultHeader("SOAPAction", ws.action ?: "\"\"")
            .build()
    }
}