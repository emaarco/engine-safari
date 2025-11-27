package de.emaarco.example.adapter.inbound.camunda

import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.domain.SubscriptionId
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Component
import java.util.*

@Component
class AbortRegistrationDelegate(
    private val useCase: AbortSubscriptionUseCase
) : BaseDelegate() {

    override fun executeTask(execution: DelegateExecution) {
        val subscriptionId = execution.getVariable("subscriptionId") as String
        log.debug { "Received task to abort registration for subscription: $subscriptionId" }
        useCase.abort(SubscriptionId(UUID.fromString(subscriptionId)))
    }
} 