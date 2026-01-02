package de.emaarco.example.adapter.inbound.camunda

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.domain.SubscriptionId
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker
import dev.bpmcrafters.processengine.worker.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class AbortRegistrationWorker(
    private val useCase: AbortSubscriptionUseCase
) {
    private val log = KotlinLogging.logger {}

    @ProcessEngineWorker(topic = TaskTypes.ABORT_REGISTRATION)
    fun abortRegistration(@Variable subscriptionId: String): Map<String, Any> {
        log.debug { "Received task to abort registration for subscription: $subscriptionId" }
        useCase.abort(SubscriptionId(UUID.fromString(subscriptionId)))
        return emptyMap()
    }
}
