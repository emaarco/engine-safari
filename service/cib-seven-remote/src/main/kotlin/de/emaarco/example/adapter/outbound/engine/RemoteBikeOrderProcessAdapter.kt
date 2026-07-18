package de.emaarco.example.adapter.outbound.engine

import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.Elements
import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.Messages
import de.emaarco.example.adapter.process.BikeOrderProcessProcessApi.PROCESS_ID
import de.emaarco.example.application.port.outbound.BikeOrderProcess
import de.emaarco.example.domain.OrderId
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * Talks to the *remote* CIB Seven engine over its REST API (`/engine-rest`):
 * starts the bike-order process and completes its user tasks.
 * The service-tasks are handled separately via the external-task client.
 */
@Component
class RemoteBikeOrderProcessAdapter(
    private val engineRestClient: RestClient,
) : BikeOrderProcess {

    private val log = KotlinLogging.logger {}

    override fun startOrder(orderTotal: Long): OrderId {
        val body = mapOf(
            "variables" to mapOf(
                "orderTotal" to mapOf("value" to orderTotal, "type" to "Long"),
            ),
        )
        val instance = engineRestClient.post()
            .uri("/process-definition/key/{key}/start", PROCESS_ID.value)
            .body(body)
            .retrieve()
            .body<ProcessInstanceDto>()
            ?: error("Engine did not return a process instance for ${PROCESS_ID.value}")
        log.info { "Started remote process instance ${instance.id}" }
        return OrderId(instance.id)
    }

    override fun completeManagerApproval(orderId: OrderId) {
        completeUserTask(orderId, Elements.TASK_MANAGER_APPROVAL.value)
    }

    override fun completeBikePreparation(orderId: OrderId) {
        completeUserTask(orderId, Elements.TASK_PREPARE_BIKE.value)
    }

    override fun broadcastPaymentCharged() {
        val messageName = Messages.MESSAGE_PAYMENT_CHARGED.value
        engineRestClient.post()
            .uri("/message")
            // `all = true` correlates to every waiting instance and does not error when none wait.
            .body(mapOf("messageName" to messageName, "all" to true))
            .retrieve()
            .toBodilessEntity()
        log.debug { "Broadcasted '$messageName' message to the remote engine" }
    }

    override fun reportDefect(orderId: OrderId) {
        val messageName = Messages.MESSAGE_DEFECT_DISCOVERED.value
        engineRestClient.post()
            .uri("/message")
            .body(mapOf("messageName" to messageName, "processInstanceId" to orderId.value))
            .retrieve()
            .toBodilessEntity()
        log.info { "Reported defect for order ${orderId.value} (correlated '$messageName')" }
    }

    private fun completeUserTask(orderId: OrderId, taskDefinitionKey: String) {
        val taskId = findTaskId(orderId, taskDefinitionKey)
        engineRestClient.post()
            .uri("/task/{id}/complete", taskId)
            .body(mapOf("variables" to emptyMap<String, Any>()))
            .retrieve()
            .toBodilessEntity()
        log.info { "Completed task '$taskDefinitionKey' ($taskId) of order ${orderId.value}" }
    }

    private fun findTaskId(orderId: OrderId, taskDefinitionKey: String): String {
        val tasks = engineRestClient.get()
            .uri {
                it.path("/task")
                    .queryParam("processInstanceId", orderId.value)
                    .queryParam("taskDefinitionKey", taskDefinitionKey)
                    .build()
            }
            .retrieve()
            .body(object : ParameterizedTypeReference<List<TaskDto>>() {})
            ?: emptyList()
        return tasks.firstOrNull()?.id
            ?: error("No active task '$taskDefinitionKey' found for order ${orderId.value}")
    }

    private data class ProcessInstanceDto(val id: String)

    private data class TaskDto(val id: String)
}
