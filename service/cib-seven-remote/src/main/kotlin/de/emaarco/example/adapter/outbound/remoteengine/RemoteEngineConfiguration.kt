package de.emaarco.example.adapter.outbound.remoteengine

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RemoteEngineConfiguration {

    /**
     * A [RestClient] pointed at the remote CIB Seven engine's REST API.
     * Used to start the process and complete its user tasks over HTTP.
     */
    @Bean
    fun engineRestClient(
        @Value("\${bike-order.engine-rest-base-url}") baseUrl: String,
    ): RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()
}
