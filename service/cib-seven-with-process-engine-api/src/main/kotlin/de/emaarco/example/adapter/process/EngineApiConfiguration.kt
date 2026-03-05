package de.emaarco.example.adapter.process

import dev.bpmcrafters.processengineapi.adapter.cibseven.embedded.shared.EngineCommandExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineApiConfiguration {

    /**
     * Executes commands to the engine via the process-engine-api.
     * Makes sure that commands are executed in the same thread as the calling operation.
     * This guarantees combined commits and rollbacks for engine and business-data.
     * If this default executor is not used, engine and business-data may diverge.
     */
    @Bean
    fun engineCommandExecutor() = EngineCommandExecutor(
        executor = { it.run() }
    )
}