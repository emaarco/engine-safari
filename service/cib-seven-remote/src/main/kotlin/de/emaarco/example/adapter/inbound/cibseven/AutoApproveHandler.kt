package de.emaarco.example.adapter.inbound.cibseven

import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.ServiceTasks
import de.emaarco.example.application.port.inbound.BikeLeasingUseCase
import de.emaarco.example.domain.OrderId
import org.cibseven.bpm.client.spring.annotation.ExternalTaskSubscription
import org.cibseven.bpm.client.task.ExternalTask
import org.cibseven.bpm.client.task.ExternalTaskService
import org.springframework.stereotype.Component

@Component
@ExternalTaskSubscription(topicName = ServiceTasks.BIKE_LEASING_AUTO_APPROVE)
class AutoApproveHandler(
    private val useCase: BikeLeasingUseCase,
) : BaseExternalTaskHandler() {

    override fun executeTask(
        orderId: OrderId,
        externalTask: ExternalTask,
        externalTaskService: ExternalTaskService,
    ) {
        val approved = useCase.autoApprove(orderId)
        externalTaskService.complete(externalTask, mapOf("approved" to approved))
    }
}
