package de.emaarco.example.adapter.outbound.camunda

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi
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
            NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED,
            variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        val message = NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
        runtimeService.createMessageCorrelation(message)
            .processInstanceVariableEquals("subscriptionId", id.value.toString())
            .correlate()
    }
} 