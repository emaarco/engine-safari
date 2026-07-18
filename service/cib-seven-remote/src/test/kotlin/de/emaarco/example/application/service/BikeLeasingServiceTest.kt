package de.emaarco.example.application.service

import de.emaarco.example.domain.OrderId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class BikeLeasingServiceTest {

    private val service = BikeLeasingService()
    private val orderId = OrderId("order-1")

    @Test
    fun `auto-approve approves the order`() {
        assertThat(service.autoApprove(orderId)).isTrue()
    }

    @Test
    fun `the remaining steps run without failing`() {
        assertThatCode {
            service.sendReminder(orderId)
            service.chargePayment(orderId)
            service.shipOrder(orderId)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `generate discount code returns a non-blank code`() {
        assertThat(service.generateDiscountCode(orderId)).isNotBlank()
    }

    @Test
    fun `notifying the customer with a discount code runs without failing`() {
        assertThatCode {
            service.notifyCustomer(orderId, service.generateDiscountCode(orderId))
        }.doesNotThrowAnyException()
    }
}
