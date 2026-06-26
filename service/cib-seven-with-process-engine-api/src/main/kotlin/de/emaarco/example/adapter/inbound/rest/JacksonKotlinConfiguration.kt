package de.emaarco.example.adapter.inbound.rest

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Registers the Jackson Kotlin module on the Spring MVC HTTP message converters.
 *
 * Under Spring Boot 4 the JSON request bodies of this app are still handled by the
 * (now deprecated) Jackson 2 `AbstractJackson2HttpMessageConverter` contributed by the
 * CIB Seven webapp. That converter uses a bare ObjectMapper without the Kotlin module, so
 * binding a body to a Kotlin data class (no no-arg constructor) fails with
 * `InvalidDefinitionException: no Creators ... exist`.
 *
 * On Spring Boot 3 the Kotlin module was applied transitively; on Boot 4 it is not, so we
 * register it explicitly on every Jackson 2 converter.
 */
@Configuration
class JacksonKotlinConfiguration : WebMvcConfigurer {

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters
            .filterIsInstance<AbstractJackson2HttpMessageConverter>()
            .forEach { it.objectMapper.registerModule(KotlinModule.Builder().build()) }
    }
}
