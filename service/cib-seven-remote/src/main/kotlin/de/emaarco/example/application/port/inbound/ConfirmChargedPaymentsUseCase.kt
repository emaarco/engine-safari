package de.emaarco.example.application.port.inbound

/**
 * Confirms charged payments back to the remote engine: correlates the `Payment charged`
 * message to every instance parked at that catch event. Triggered periodically by a scheduler.
 */
interface ConfirmChargedPaymentsUseCase {
    fun confirmChargedPayments()
}
