package de.emaarco.example.adapter.outbound.camunda

import de.emaarco.example.application.port.outbound.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import org.camunda.bpm.engine.RuntimeService
import org.springframework.stereotype.Component

@Component
class NewsletterSubscriptionProcessAdapter(
    private val runtimeService: RuntimeService
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        val variables = mapOf("subscriptionId" to id.value.toString())
        runtimeService.startProcessInstanceByMessage(
            "Message_FormSubmitted",
            variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        runtimeService.createMessageCorrelation("Message_SubscriptionConfirmed")
            .processInstanceVariableEquals("subscriptionId", id.value.toString())
            .correlate()
    }
} 