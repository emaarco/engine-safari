package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.ConfirmChargedPaymentsUseCase
import de.emaarco.example.application.port.outbound.BikeOrderProcess
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Process-out logic: confirms charged payments by correlating the `Payment charged`
 * message back into the remote engine. Simulates an async payment-provider confirmation
 * that arrives out-of-band after the *Charge Payment* task ran.
 */
@Service
class PaymentConfirmationService(
    private val process: BikeOrderProcess,
) : ConfirmChargedPaymentsUseCase {

    private val log = KotlinLogging.logger {}

    override fun confirmChargedPayments() {
        log.debug { "Confirming charged payments – broadcasting 'Payment charged' to the engine" }
        process.broadcastPaymentCharged()
    }
}
