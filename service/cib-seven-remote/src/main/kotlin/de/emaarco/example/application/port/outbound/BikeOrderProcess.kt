package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.OrderId

/**
 * Outbound port to the remote CIB Seven engine (spoken via its REST API).
 * Used to start the process and to complete its user tasks remotely.
 */
interface BikeOrderProcess {
    fun startOrder(orderTotal: Long): OrderId
    fun completeManagerApproval(orderId: OrderId)
    fun completeBikePreparation(orderId: OrderId)
}
