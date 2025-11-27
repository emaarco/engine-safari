package de.emaarco.example.adapter.inbound.operaton

import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.domain.SubscriptionId
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailDelegate(
    private val useCase: SendConfirmationMailUseCase
) : BaseDelegate() {

    override fun executeTask(execution: DelegateExecution) {
        val subscriptionId = execution.getVariable("subscriptionId") as String
        log.debug { "Received task to send confirmation mail for subscription: $subscriptionId" }
        useCase.sendConfirmationMail(SubscriptionId(UUID.fromString(subscriptionId)))
    }
} 