package de.emaarco.example.adapter.inbound.cibseven

import de.emaarco.example.domain.OrderId
import mu.KotlinLogging
import org.cibseven.bpm.client.task.ExternalTask
import org.cibseven.bpm.client.task.ExternalTaskHandler
import org.cibseven.bpm.client.task.ExternalTaskService

/**
 * Base for all remote external-task handlers.
 * Wraps the actual work in a try/catch and reports failures back to the
 * remote engine via [ExternalTaskService.handleFailure] (no retries here).
 */
abstract class BaseExternalTaskHandler : ExternalTaskHandler {

    protected val log = KotlinLogging.logger {}

    override fun execute(externalTask: ExternalTask, externalTaskService: ExternalTaskService) {
        val orderId = OrderId(externalTask.processInstanceId)
        try {
            executeTask(orderId, externalTask, externalTaskService)
        } catch (e: Exception) {
            log.error(e) { "Error while processing external task '${externalTask.topicName}'" }
            externalTaskService.handleFailure(
                externalTask,
                e.message ?: "Error while processing external task",
                e.stackTraceToString(),
                0,
                0L,
            )
        }
    }

    abstract fun executeTask(
        orderId: OrderId,
        externalTask: ExternalTask,
        externalTaskService: ExternalTaskService,
    )
}
