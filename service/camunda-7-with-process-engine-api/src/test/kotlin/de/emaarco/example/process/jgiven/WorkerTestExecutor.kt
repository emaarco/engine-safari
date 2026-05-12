package de.emaarco.example.process.jgiven

import dev.bpmcrafters.processengine.worker.BpmnErrorOccurred
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker
import dev.bpmcrafters.processengine.worker.Variable
import org.camunda.bpm.engine.ProcessEngine
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

/**
 * Drives `@ProcessEngineWorker`-annotated functions synchronously for tests.
 * Bypasses the bpm-crafters poll loop by fetching external tasks for a topic,
 * invoking the worker method with the resolved `@Variable` arguments,
 * and completing the task with the returned variables map.
 */
class WorkerTestExecutor(private val processEngine: ProcessEngine) {

    private data class Registration(val worker: Any, val function: KFunction<*>)

    private val registrations = mutableMapOf<String, Registration>()

    fun registerWorkers(vararg workers: Any) {
        workers.forEach { worker ->
            worker::class.functions
                .filter { it.findAnnotation<ProcessEngineWorker>() != null }
                .forEach { fn ->
                    val topic = fn.findAnnotation<ProcessEngineWorker>()!!.topic
                    registrations[topic] = Registration(worker, fn)
                }
        }
    }

    fun executeWorker(topic: String) {
        val registration = registrations[topic]
            ?: error("No worker registered for topic '$topic'")

        val tasks = processEngine.externalTaskService
            .fetchAndLock(1, WORKER_ID)
            .topic(topic, LOCK_DURATION_MS)
            .execute()
        check(tasks.isNotEmpty()) { "No external task waiting on topic '$topic'" }
        val task = tasks.first()
        val processVariables = processEngine.runtimeService.getVariables(task.processInstanceId)
        val callArgs = buildMap {
            put(registration.function.parameters[0], registration.worker)
            registration.function.parameters.drop(1).forEach { kParam ->
                val variableName = kParam.findAnnotation<Variable>()
                    ?.let { ann -> ann.name.takeIf { it.isNotBlank() && it != "__unnamed" } }
                    ?: kParam.name?.takeIf { it != "__unnamed" }
                    ?: error(
                        "Cannot resolve variable name for parameter on '$topic'. " +
                            "Either specify @Variable(name = \"...\") or compile with -java-parameters.",
                    )
                put(kParam, processVariables[variableName])
            }
        }

        val result = try {
            registration.function.callBy(callArgs)
        } catch (e: Exception) {
            val cause = e.cause ?: e
            if (cause is BpmnErrorOccurred) {
                processEngine.externalTaskService.handleBpmnError(task.id, WORKER_ID, cause.errorCode)
                return
            }
            throw cause
        }

        @Suppress("UNCHECKED_CAST")
        val variables = result as? Map<String, Any> ?: emptyMap()
        processEngine.externalTaskService.complete(task.id, WORKER_ID, variables)
    }

    private companion object {
        const val WORKER_ID = "test-worker"
        const val LOCK_DURATION_MS = 30_000L
    }
}
