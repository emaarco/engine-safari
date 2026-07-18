package de.emaarco.example.adapter.inbound.cibseven

import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.ServiceTasks
import de.emaarco.example.application.port.inbound.BikeLeasingUseCase
import de.emaarco.example.domain.OrderId
import org.cibseven.bpm.client.spring.annotation.ExternalTaskSubscription
import org.cibseven.bpm.client.task.ExternalTask
import org.cibseven.bpm.client.task.ExternalTaskService
import org.springframework.stereotype.Component

/**
 * Escalation worker: informs the customer that they need to pick a new bike, handing over the
 * `discountCode` produced by [GenerateDiscountHandler] (e.g. via mail). Last step before the
 * order is cancelled.
 */
@Component
@ExternalTaskSubscription(
    topicName = ServiceTasks.BIKE_LEASING_NOTIFY_CUSTOMER,
    variableNames = ["discountCode"],
)
class NotifyCustomerHandler(
    private val useCase: BikeLeasingUseCase,
) : BaseExternalTaskHandler() {

    override fun executeTask(
        orderId: OrderId,
        externalTask: ExternalTask,
        externalTaskService: ExternalTaskService,
    ) {
        val discountCode = externalTask.getVariable<String>("discountCode")
        useCase.notifyCustomer(orderId, discountCode)
        externalTaskService.complete(externalTask)
    }
}
