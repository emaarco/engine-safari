package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.OrderId

/**
 * Outbound port to the remote CIB Seven engine (spoken via its REST API).
 * Used to start the process, complete its user tasks and send messages back into it.
 */
interface BikeOrderProcess {
    fun startOrder(orderTotal: Long): OrderId
    fun completeManagerApproval(orderId: OrderId)
    fun completeBikePreparation(orderId: OrderId)
    fun broadcastPaymentCharged()

    /** Reports a defect for a specific order – triggers the escalation boundary event on *Prepare Bike*. */
    fun reportDefect(orderId: OrderId)
}
