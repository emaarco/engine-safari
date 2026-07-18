package de.emaarco.example.adapter.inbound.scheduler

import de.emaarco.example.application.port.inbound.ConfirmChargedPaymentsUseCase
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Inbound *process-out* trigger: every few seconds it asks the engine to move on every instance
 * currently parked at the `Payment charged` message event. Mirrors a real payment provider that
 * would confirm charges asynchronously – here a simple poll stands in for the webhook.
 */
@Component
class PaymentConfirmationScheduler(
    private val useCase: ConfirmChargedPaymentsUseCase,
) {

    private val log = KotlinLogging.logger {}

    @Scheduled(fixedRateString = "\${bike-order.payment-confirmation.poll-interval:10000}")
    fun confirmChargedPayments() {
        log.debug { "Polling for orders awaiting payment confirmation" }
        useCase.confirmChargedPayments()
    }
}
