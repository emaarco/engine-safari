package de.emaarco.example.process.basic

import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.Elements
import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.Messages
import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.PROCESS_ID
import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.ServiceTasks
import de.emaarco.example.process.basic.util.completeExternalTask
import de.emaarco.example.process.basic.util.fireTimer
import org.cibseven.bpm.engine.ProcessEngine
import org.cibseven.bpm.engine.RuntimeService
import org.cibseven.bpm.engine.runtime.ProcessInstance
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.init
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Process test for the bike-order model that is deployed in this embedded engine but automated
 * remotely by the `cib-seven-remote` worker service. Since no worker runs here, the test drives the
 * external service-tasks itself via [completeExternalTask] and the user tasks via bpm-assert.
 */
@SpringBootTest
@ActiveProfiles("test")
class BikeOrderProcessTest {

    @Autowired
    private lateinit var runtimeService: RuntimeService

    @Autowired
    private lateinit var processEngine: ProcessEngine

    @BeforeEach
    fun setUp() {
        init(processEngine)
    }

    private fun startOrder(orderTotal: Long): ProcessInstance =
        runtimeService.startProcessInstanceByKey(PROCESS_ID.value, mapOf("orderTotal" to orderTotal))

    /** Mirrors the remote `PaymentConfirmationScheduler` correlating the `Payment charged` message. */
    private fun confirmPaymentCharged(instance: ProcessInstance) =
        runtimeService.createMessageCorrelation(Messages.MESSAGE_PAYMENT_CHARGED.value)
            .processInstanceId(instance.id)
            .correlate()

    /** Mirrors the remote `reportDefect` endpoint correlating the `Defect discovered` message. */
    private fun reportDefect(instance: ProcessInstance) =
        runtimeService.createMessageCorrelation(Messages.MESSAGE_DEFECT_DISCOVERED.value)
            .processInstanceId(instance.id)
            .correlate()

    @Test
    fun `auto-approve path - order below threshold is shipped without a manager`() {
        val instance = startOrder(500)

        assertThat(instance).isWaitingAt(Elements.TASK_AUTO_APPROVE.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_AUTO_APPROVE, mapOf("approved" to true))

        assertThat(instance).isWaitingAt(Elements.TASK_PREPARE_BIKE.value)
        complete(task(instance))

        assertThat(instance).isWaitingAt(Elements.TASK_CHARGE_PAYMENT.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_CHARGE_PAYMENT)

        assertThat(instance).isWaitingAt(Elements.EVENT_PAYMENT_CHARGED.value)
        confirmPaymentCharged(instance)

        assertThat(instance).isWaitingAt(Elements.TASK_SHIP_ORDER.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_SHIP_ORDER)

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.START_EVENT_ORDER_RECEIVED.value,
                Elements.TASK_AUTO_APPROVE.value,
                Elements.TASK_PREPARE_BIKE.value,
                Elements.TASK_CHARGE_PAYMENT.value,
                Elements.EVENT_PAYMENT_CHARGED.value,
                Elements.TASK_SHIP_ORDER.value,
                Elements.END_EVENT_SHIPPED.value,
            )
            .hasNotPassed(
                Elements.TASK_MANAGER_APPROVAL.value,
                Elements.TASK_SEND_REMINDER.value,
                Elements.END_EVENT_REMINDER_SENT.value,
            )
    }

    @Test
    fun `manager-approval path - high-value order requires manager approval`() {
        val instance = startOrder(1500)

        assertThat(instance).isWaitingAt(Elements.TASK_MANAGER_APPROVAL.value)
        complete(task(instance))

        assertThat(instance).isWaitingAt(Elements.TASK_PREPARE_BIKE.value)
        complete(task(instance))

        assertThat(instance).isWaitingAt(Elements.TASK_CHARGE_PAYMENT.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_CHARGE_PAYMENT)

        assertThat(instance).isWaitingAt(Elements.EVENT_PAYMENT_CHARGED.value)
        confirmPaymentCharged(instance)

        assertThat(instance).isWaitingAt(Elements.TASK_SHIP_ORDER.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_SHIP_ORDER)

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.START_EVENT_ORDER_RECEIVED.value,
                Elements.TASK_MANAGER_APPROVAL.value,
                Elements.TASK_PREPARE_BIKE.value,
                Elements.TASK_CHARGE_PAYMENT.value,
                Elements.EVENT_PAYMENT_CHARGED.value,
                Elements.TASK_SHIP_ORDER.value,
                Elements.END_EVENT_SHIPPED.value,
            )
            .hasNotPassed(Elements.TASK_AUTO_APPROVE.value)
    }

    @Test
    fun `reminder path - non-interrupting timer sends a reminder while approval is pending`() {
        val instance = startOrder(1500)

        assertThat(instance).isWaitingAt(Elements.TASK_MANAGER_APPROVAL.value)

        // Non-interrupting boundary timer -> spawns the reminder external task, approval keeps waiting.
        processEngine.fireTimer(Elements.BOUNDARY_EVENT_REMINDER)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_SEND_REMINDER)

        assertThat(instance)
            .isWaitingAt(Elements.TASK_MANAGER_APPROVAL.value)
            .hasPassed(
                Elements.TASK_SEND_REMINDER.value,
                Elements.END_EVENT_REMINDER_SENT.value,
            )

        // Finish the main flow.
        complete(task(instance))
        complete(task(instance))
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_CHARGE_PAYMENT)
        confirmPaymentCharged(instance)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_SHIP_ORDER)

        assertThat(instance)
            .isEnded
            .hasPassed(
                Elements.BOUNDARY_EVENT_REMINDER.value,
                Elements.TASK_SEND_REMINDER.value,
                Elements.END_EVENT_REMINDER_SENT.value,
                Elements.EVENT_PAYMENT_CHARGED.value,
                Elements.END_EVENT_SHIPPED.value,
            )
    }

    @Test
    fun `defect path - reporting a defect during preparation cancels the order with a discount`() {
        val instance = startOrder(500)

        assertThat(instance).isWaitingAt(Elements.TASK_AUTO_APPROVE.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_AUTO_APPROVE, mapOf("approved" to true))

        // A defect is discovered while preparing the bike -> interrupting boundary event escalates.
        assertThat(instance).isWaitingAt(Elements.TASK_PREPARE_BIKE.value)
        reportDefect(instance)

        assertThat(instance).isWaitingAt(Elements.TASK_GENERATE_DISCOUNT.value)
        processEngine.completeExternalTask(
            ServiceTasks.BIKE_LEASING_GENERATE_DISCOUNT_CODE,
            mapOf("discountCode" to "MIRAVELO-TEST01"),
        )

        assertThat(instance).isWaitingAt(Elements.TASK_NOTIFY_CUSTOMER.value)
        processEngine.completeExternalTask(ServiceTasks.BIKE_LEASING_NOTIFY_CUSTOMER)

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.TASK_PREPARE_BIKE.value,
                Elements.BOUNDARY_EVENT_DEFECT_DISCOVERED.value,
                Elements.TASK_GENERATE_DISCOUNT.value,
                Elements.TASK_NOTIFY_CUSTOMER.value,
                Elements.END_EVENT_ORDER_CANCELLED.value,
            )
            .hasNotPassed(
                Elements.TASK_CHARGE_PAYMENT.value,
                Elements.TASK_SHIP_ORDER.value,
                Elements.END_EVENT_SHIPPED.value,
            )
    }
}
