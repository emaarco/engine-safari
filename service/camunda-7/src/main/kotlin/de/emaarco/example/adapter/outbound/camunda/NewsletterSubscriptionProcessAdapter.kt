package de.emaarco.example.adapter.outbound.camunda

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Variables
import de.emaarco.example.application.port.outbound.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import org.camunda.bpm.engine.RuntimeService
import org.springframework.stereotype.Component

@Component
class NewsletterSubscriptionProcessAdapter(
    private val runtimeService: RuntimeService
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        val variables = mapOf(Variables.ActivitySendConfirmationMail.SUBSCRIPTION_ID.value to id.value.toString())
        runtimeService.startProcessInstanceByMessage(
            Messages.MESSAGE_FORM_SUBMITTED.value,
            variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        runtimeService.createMessageCorrelation(Messages.MESSAGE_SUBSCRIPTION_CONFIRMED.value)
            .processInstanceVariableEquals(Variables.ActivityConfirmRegistration.SUBSCRIPTION_ID.value, id.value.toString())
            .correlate()
    }
} 