package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.BikeLeasingUseCase
import de.emaarco.example.domain.OrderId
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * The 'business logic' of the bike-leasing service-tasks.
 * In this example the steps just log – the point is that they run in a
 * completely separate service and are triggered as remote external tasks.
 */
@Service
class BikeLeasingService : BikeLeasingUseCase {

    private val log = KotlinLogging.logger {}

    override fun autoApprove(orderId: OrderId): Boolean {
        log.info { "Auto-approving order ${orderId.value} (below approval threshold)" }
        return true
    }

    override fun sendReminder(orderId: OrderId) {
        log.info { "Sending approval reminder for order ${orderId.value}" }
    }

    override fun chargePayment(orderId: OrderId) {
        log.info { "Charging payment for order ${orderId.value}" }
    }

    override fun shipOrder(orderId: OrderId) {
        log.info { "Shipping bike for order ${orderId.value}" }
    }

    override fun generateDiscountCode(orderId: OrderId): String {
        val code = "MIRAVELO-${orderId.value.takeLast(6).uppercase()}"
        log.info { "Defect found on order ${orderId.value} – generated discount code $code" }
        return code
    }

    override fun notifyCustomer(orderId: OrderId, discountCode: String) {
        log.info {
            "Notifying customer of order ${orderId.value}: please pick a new bike, " +
                "here is your discount code $discountCode"
        }
    }
}
