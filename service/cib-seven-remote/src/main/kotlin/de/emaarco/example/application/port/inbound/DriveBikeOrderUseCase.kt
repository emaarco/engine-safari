package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.OrderId

/**
 * Drives the bike-order process on the remote engine:
 * starting instances and completing the human (user) tasks.
 */
interface DriveBikeOrderUseCase {
    fun startOrder(orderTotal: Long): OrderId
    fun approveByManager(orderId: OrderId)
    fun completeBikePreparation(orderId: OrderId)

    /** Reports a defect discovered while preparing the bike – escalates and cancels the order. */
    fun reportDefect(orderId: OrderId)
}
