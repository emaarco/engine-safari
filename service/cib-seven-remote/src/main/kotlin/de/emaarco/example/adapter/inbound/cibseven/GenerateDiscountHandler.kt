package de.emaarco.example.adapter.inbound.cibseven

import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.ServiceTasks
import de.emaarco.example.application.port.inbound.BikeLeasingUseCase
import de.emaarco.example.domain.OrderId
import org.cibseven.bpm.client.spring.annotation.ExternalTaskSubscription
import org.cibseven.bpm.client.task.ExternalTask
import org.cibseven.bpm.client.task.ExternalTaskService
import org.springframework.stereotype.Component

/**
 * Escalation worker: after a defect was reported during *Prepare Bike*, mints a discount code
 * and hands it to the process as the `discountCode` variable for the follow-up notification.
 */
@Component
@ExternalTaskSubscription(topicName = ServiceTasks.BIKE_LEASING_GENERATE_DISCOUNT_CODE)
class GenerateDiscountHandler(
    private val useCase: BikeLeasingUseCase,
) : BaseExternalTaskHandler() {

    override fun executeTask(
        orderId: OrderId,
        externalTask: ExternalTask,
        externalTaskService: ExternalTaskService,
    ) {
        val discountCode = useCase.generateDiscountCode(orderId)
        externalTaskService.complete(externalTask, mapOf("discountCode" to discountCode))
    }
}
