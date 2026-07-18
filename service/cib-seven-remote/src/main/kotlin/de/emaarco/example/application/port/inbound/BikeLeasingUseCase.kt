package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.OrderId

/**
 * Business logic behind the external service-tasks of the bike-order process.
 * Triggered by the remote external-task workers.
 */
interface BikeLeasingUseCase {
    fun autoApprove(orderId: OrderId): Boolean
    fun sendReminder(orderId: OrderId)
    fun chargePayment(orderId: OrderId)
    fun shipOrder(orderId: OrderId)

    /** Escalation: a defect was found while preparing the bike – mint a discount code for the customer. */
    fun generateDiscountCode(orderId: OrderId): String

    /** Escalation: tell the customer to pick a new bike, handing them the discount code (e.g. via mail). */
    fun notifyCustomer(orderId: OrderId, discountCode: String)
}
