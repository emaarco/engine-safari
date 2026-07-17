package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.DriveBikeOrderUseCase
import de.emaarco.example.application.port.outbound.BikeOrderProcess
import de.emaarco.example.domain.OrderId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DriveBikeOrderService(
    private val process: BikeOrderProcess,
) : DriveBikeOrderUseCase {

    private val log = KotlinLogging.logger {}

    override fun startOrder(orderTotal: Long): OrderId {
        log.info { "Starting a new bike order with total $orderTotal" }
        return process.startOrder(orderTotal)
    }

    override fun approveByManager(orderId: OrderId) {
        log.info { "Completing 'Manager Approval' for order ${orderId.value}" }
        process.completeManagerApproval(orderId)
    }

    override fun completeBikePreparation(orderId: OrderId) {
        log.info { "Completing 'Prepare Bike' for order ${orderId.value}" }
        process.completeBikePreparation(orderId)
    }
}
