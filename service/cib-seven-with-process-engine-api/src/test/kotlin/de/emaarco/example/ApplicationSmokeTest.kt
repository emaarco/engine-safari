package de.emaarco.example

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.UUID

/**
 * Full-stack smoke test that boots the complete application on Spring Boot 4:
 * embedded CIB Seven engine + webapp + the bpm-crafters process-engine adapter
 * (cib-seven-embedded) + the REST layer, all on an in-memory H2 database.
 *
 * This proves on every CI run that:
 *  - the application context starts on Spring Boot 4 (catches boot-time auto-configuration /
 *    classpath regressions in the engine, webapp and adapter starters), and
 *  - the REST layer can deserialize a Kotlin `data class` request body and start a process
 *    instance through the adapter (catches the Spring Boot 4 Jackson-Kotlin regression).
 *
 * Unlike the process-test suite (which boots with the webapp disabled), this test exercises
 * the same Spring MVC path a real client / Bruno request would.
 */
@SpringBootTest
@ActiveProfiles("smoketest")
class ApplicationSmokeTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `subscribe endpoint starts a process and returns a subscription id`() {
        val body = """
            {
              "email": "smoke@mail.com",
              "name": "Smoke Test",
              "newsletterId": "${UUID.randomUUID()}"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/subscriptions/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subscriptionId").isNotEmpty)
    }
}
