package de.emaarco.example.process.jgiven

import com.fasterxml.jackson.databind.ObjectMapper
import dev.bpmcrafters.processengine.worker.BpmnErrorOccurred
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker
import dev.bpmcrafters.processengine.worker.Variable
import dev.bpmcrafters.processengine.worker.Variable.Companion.DEFAULT_UNNAMED_NAME
import dev.bpmcrafters.processengineapi.adapter.cibseven.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.task.TaskInformation
import org.cibseven.bpm.engine.ProcessEngine
import org.cibseven.bpm.engine.externaltask.LockedExternalTask
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

/**
 * Drives `@ProcessEngineWorker`-annotated functions synchronously for tests, bypassing the
 * bpm-crafters poll loop. For a given topic it fetches the waiting external task, resolves the
 * worker's parameters from the *task-delivered* variables — converting each value to the declared
 * parameter type just like the real worker runtime does — invokes the worker, and then either
 * completes the task or translates a thrown [BpmnErrorOccurred] into an engine BPMN error.
 *
 * Mirrors production worker behaviour closely:
 * - honours `@ProcessEngineWorker(autoComplete = false)` by leaving completion to the worker,
 * - supports `@Variable` parameters and a [TaskInformation] parameter,
 * - propagates the BPMN error's message and payload to the engine.
 */
class WorkerTestExecutor(private val processEngine: ProcessEngine) {

    private data class Registration(
        val worker: Any,
        val function: KFunction<*>,
        val annotation: ProcessEngineWorker,
    )

    private val registrations = mutableMapOf<String, Registration>()
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val externalTaskService = processEngine.externalTaskService

    fun registerWorkers(vararg workers: Any) {
        workers.forEach { worker ->
            worker::class.functions
                .mapNotNull { fn -> fn.findAnnotation<ProcessEngineWorker>()?.let { fn to it } }
                .forEach { (fn, annotation) -> registrations[annotation.topic] = Registration(worker, fn, annotation) }
        }
    }

    fun executeWorker(topic: String) {
        val registration = registrations[topic] ?: error("No worker registered for topic '$topic'")
        val task = fetchTask(topic)
        val callArgs = resolveArguments(registration, task)

        val result = try {
            registration.function.callBy(callArgs)
        } catch (e: Exception) {
            val cause = e.cause ?: e
            if (cause is BpmnErrorOccurred) {
                externalTaskService.handleBpmnError(task.id, WORKER_ID, cause.errorCode, cause.message, cause.payload)
                return
            }
            throw cause
        }

        if (!registration.annotation.autoComplete) return
        val variables = (result as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value } ?: emptyMap()
        externalTaskService.complete(task.id, WORKER_ID, variables)
    }

    private fun fetchTask(topic: String): LockedExternalTask {
        val tasks = externalTaskService
            .fetchAndLock(1, WORKER_ID)
            .topic(topic, LOCK_DURATION_MS)
            .enableCustomObjectDeserialization()
            .execute()
        check(tasks.isNotEmpty()) {
            val waiting = externalTaskService.createExternalTaskQuery().list().joinToString(", ") { it.topicName }
            "No external task waiting on topic '$topic'. Waiting topics: [$waiting]"
        }
        return tasks.first()
    }

    private fun resolveArguments(registration: Registration, task: LockedExternalTask): Map<KParameter, Any?> =
        buildMap {
            put(registration.function.parameters[0], registration.worker)
            registration.function.parameters.drop(1).forEach { param -> put(param, resolveParameter(param, task)) }
        }

    private fun resolveParameter(param: KParameter, task: LockedExternalTask): Any? {
        if (param.type.jvmErasure.java == TaskInformation::class.java) return task.toTaskInformation()
        val variableName = resolveVariableName(param, task.topicName)
        return convert(task.variables[variableName], param.type)
    }

    private fun resolveVariableName(param: KParameter, topic: String): String =
        param.findAnnotation<Variable>()?.name?.takeIf { it.isNotBlank() && it != DEFAULT_UNNAMED_NAME }
            ?: param.name?.takeIf { it != DEFAULT_UNNAMED_NAME }
            ?: error(
                "Cannot resolve variable name for parameter on '$topic'. " +
                    "Either specify @Variable(name = \"...\") or compile with -java-parameters.",
            )

    private fun convert(rawValue: Any?, targetType: KType): Any? {
        if (rawValue == null) return null
        val targetClass = targetType.jvmErasure.java
        return if (targetClass.isInstance(rawValue)) rawValue else objectMapper.convertValue(rawValue, targetClass)
    }

    private companion object {
        const val WORKER_ID = "test-worker"
        const val LOCK_DURATION_MS = 30_000L
    }
}
