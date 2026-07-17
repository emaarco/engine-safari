package de.emaarco.example.process.basic.util

import org.cibseven.bpm.engine.ProcessEngine

/**
 * Completes the external service-task currently waiting on [topic] by fetching, locking and
 * completing it with a throwaway worker. Mirrors what a real external-task worker would do — the
 * embedded engine has no worker of its own for the bike-order process (it is consumed remotely).
 */
fun ProcessEngine.completeExternalTask(topic: String, variables: Map<String, Any> = emptyMap()) {
    val worker = "test-worker"
    val task = externalTaskService.fetchAndLock(1, worker)
        .topic(topic, 10_000L)
        .execute()
        .firstOrNull()
        ?: error("No external task waiting on topic '$topic'")
    externalTaskService.complete(task.id, worker, variables)
}
