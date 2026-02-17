package de.emaarco.example.adapter.outbound.cibseven

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Variables
import de.emaarco.example.application.port.outbound.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import org.cibseven.bpm.engine.RuntimeService
import org.springframework.stereotype.Component

@Component
class NewsletterSubscriptionProcessAdapter(
    private val runtimeService: RuntimeService
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        val variables = mapOf("subscriptionId" to id.value.toString())
        runtimeService.startProcessInstanceByMessage(
            Messages.MESSAGE_FORM_SUBMITTED,
            variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        val message = Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
        runtimeService.createMessageCorrelation(message)
            .processInstanceVariableEquals(Variables.SUBSCRIPTION_ID, id.value.toString())
            .correlate()
    }
}
