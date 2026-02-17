package de.emaarco.example.adapter.outbound.cib7

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Variables
import de.emaarco.example.application.port.outbound.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import dev.bpmcrafters.processengineapi.CommonRestrictions
import dev.bpmcrafters.processengineapi.correlation.CorrelateMessageCmd
import dev.bpmcrafters.processengineapi.correlation.Correlation
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.process.StartProcessByMessageCmd
import org.springframework.stereotype.Component

@Component
class NewsletterSubscriptionProcessAdapter(
    private val startProcessApi: StartProcessApi,
    private val correlationApi: CorrelationApi,
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        startProcessApi.startProcess(
            cmd = StartProcessByMessageCmd(
                messageName = Messages.MESSAGE_FORM_SUBMITTED,
                payload = mapOf(
                    Variables.SUBSCRIPTION_ID to id.value.toString(),
                    CommonRestrictions.CORRELATION_KEY to id.value.toString()
                )
            )
        ).join()
    }

    override fun confirmSubscription(
        id: SubscriptionId
    ) {
        correlationApi.correlateMessage(
            cmd = CorrelateMessageCmd(
                messageName = Messages.MESSAGE_SUBSCRIPTION_CONFIRMED,
                payload = mapOf(Variables.SUBSCRIPTION_ID to id.value.toString()),
                correlation = Correlation.withKey(id.value.toString()),
                restrictions = messageEventRestrictions()
            )
        ).join()
    }

    private fun messageEventRestrictions() = CommonRestrictions.builder()
        .withRestriction("useGlobalCorrelationKey", "true")
        .build()
}
