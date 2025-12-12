package de.emaarco.example.adapter.inbound.cibseven

import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import de.emaarco.example.domain.SubscriptionId
import org.cibseven.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendWelcomeMailDelegate(
    private val useCase: SendWelcomeMailUseCase
) : BaseDelegate() {

    override fun executeTask(execution: DelegateExecution) {
        val subscriptionId = execution.getVariable("subscriptionId") as String
        log.debug { "Received task to send welcome mail for subscription: $subscriptionId" }
        useCase.sendWelcomeMail(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}
