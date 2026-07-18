package de.emaarco.example.adapter.inbound.cibseven

import de.emaarco.example.application.port.inbound.BikeLeasingUseCase
import de.emaarco.example.domain.OrderId
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.cibseven.bpm.client.task.ExternalTask
import org.cibseven.bpm.client.task.ExternalTaskService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for the remote external-task workers. They verify that each handler delegates to the
 * matching use case and completes (or fails) the external task correctly — no engine involved.
 */
class BikeOrderHandlerTest {

    private val useCase = mockk<BikeLeasingUseCase>()
    private val externalTask = mockk<ExternalTask>()
    private val externalTaskService = mockk<ExternalTaskService>(relaxed = true)

    private val orderId = OrderId("pi-1")

    @BeforeEach
    fun setUp() {
        every { externalTask.processInstanceId } returns "pi-1"
        every { externalTask.topicName } returns "some.topic"
    }

    @Test
    fun `auto-approve completes the task with the approval decision`() {
        every { useCase.autoApprove(orderId) } returns true

        AutoApproveHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { useCase.autoApprove(orderId) }
        verify(exactly = 1) { externalTaskService.complete(externalTask, mapOf("approved" to true)) }
    }

    @Test
    fun `send reminder completes the task`() {
        every { useCase.sendReminder(orderId) } just Runs

        SendReminderHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { useCase.sendReminder(orderId) }
        verify(exactly = 1) { externalTaskService.complete(externalTask) }
    }

    @Test
    fun `charge payment completes the task`() {
        every { useCase.chargePayment(orderId) } just Runs

        ChargePaymentHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { useCase.chargePayment(orderId) }
        verify(exactly = 1) { externalTaskService.complete(externalTask) }
    }

    @Test
    fun `ship order completes the task`() {
        every { useCase.shipOrder(orderId) } just Runs

        ShipOrderHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { useCase.shipOrder(orderId) }
        verify(exactly = 1) { externalTaskService.complete(externalTask) }
    }

    @Test
    fun `generate discount code completes the task with the generated code`() {
        every { useCase.generateDiscountCode(orderId) } returns "MIRAVELO-ABC123"

        GenerateDiscountHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { useCase.generateDiscountCode(orderId) }
        verify(exactly = 1) {
            externalTaskService.complete(externalTask, mapOf("discountCode" to "MIRAVELO-ABC123"))
        }
    }

    @Test
    fun `notify customer reads the discount code and completes the task`() {
        every { externalTask.getVariable<String>("discountCode") } returns "MIRAVELO-ABC123"
        every { useCase.notifyCustomer(orderId, "MIRAVELO-ABC123") } just Runs

        NotifyCustomerHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { useCase.notifyCustomer(orderId, "MIRAVELO-ABC123") }
        verify(exactly = 1) { externalTaskService.complete(externalTask) }
    }

    @Test
    fun `a failing use case is reported to the engine via handleFailure`() {
        every { useCase.shipOrder(orderId) } throws RuntimeException("boom")

        ShipOrderHandler(useCase).execute(externalTask, externalTaskService)

        verify(exactly = 1) { externalTaskService.handleFailure(externalTask, "boom", any(), 0, 0L) }
        verify(exactly = 0) { externalTaskService.complete(externalTask) }
    }
}
