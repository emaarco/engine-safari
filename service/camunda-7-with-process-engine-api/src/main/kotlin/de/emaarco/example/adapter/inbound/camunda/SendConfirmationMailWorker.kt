package de.emaarco.example.adapter.inbound.camunda

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.domain.SubscriptionId
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker
import dev.bpmcrafters.processengine.worker.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailWorker(
    private val useCase: SendConfirmationMailUseCase
) {
    private val log = KotlinLogging.logger {}

    @ProcessEngineWorker(topic = TaskTypes.SEND_CONFIRMATION_MAIL)
    fun sendConfirmationMail(@Variable subscriptionId: String) {
        log.debug { "Received task to send confirmation mail for subscription: $subscriptionId" }
        useCase.sendConfirmationMail(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}
