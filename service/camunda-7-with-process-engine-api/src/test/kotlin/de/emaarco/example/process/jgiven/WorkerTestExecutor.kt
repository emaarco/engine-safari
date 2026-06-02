package de.emaarco.example.process.jgiven

import com.fasterxml.jackson.databind.ObjectMapper
import dev.bpmcrafters.processengine.worker.BpmnErrorOccurred
import dev.bpmcrafters.processengine.worker.ProcessEngineWorker
import dev.bpmcrafters.processengine.worker.Variable
import dev.bpmcrafters.processengine.worker.Variable.Companion.DEFAULT_UNNAMED_NAME
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.task.TaskInformation
import mu.KotlinLogging
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.externaltask.LockedExternalTask
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

/**
 * Executes `@ProcessEngineWorker` functions in BPMN process tests with mocked dependencies,
 * bypassing the bpm-crafters poll loop.
 *
 * For a registered topic it: fetches the waiting external task, discovers the worker method,
 * resolves each parameter from the task-delivered variables (mirroring the real worker runtime),
 * invokes the worker, and finally either completes the external task or — if the worker raised a
 * [BpmnErrorOccurred] — reports the BPMN error to the engine.
 *
 * Example:
 * ```
 * val executor = WorkerTestExecutor(processEngine)
 * executor.registerWorker(SendConfirmationMailWorker(useCase)) // topic taken from the annotation
 * executor.executeWorker(ServiceTasks.SEND_CONFIRMATION_MAIL)
 * ```
 */
class WorkerTestExecutor(private val processEngine: ProcessEngine) {

    private val log = KotlinLogging.logger {}
    private val registeredWorkers = mutableMapOf<String, Any>()
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val externalTaskService = processEngine.externalTaskService

    fun <T : Any> registerWorkers(vararg workers: T): List<T> = workers.map { registerWorker(it) }

    fun <T : Any> registerWorker(worker: T): T {
        val topic = extractTaskTopicFromWorker(worker)
        registeredWorkers[topic] = worker
        log.debug { "Registered worker for task topic '$topic'." }
        return worker
    }

    fun executeWorker(topic: String) {
        val worker = getWorkerByTopic(topic)
        val externalTask = findExternalTask(topic)
        log.debug { "Executing worker for topic '$topic', task id '${externalTask.id}'." }

        val (workerMethod, workerAnnotation) = findWorkerMethod(worker, topic)
        val methodArguments = mapVariablesToParameters(worker, workerMethod, externalTask)

        val rawWorkerResult = try {
            workerMethod.callBy(methodArguments)
        } catch (e: Exception) {
            handleWorkerException(externalTask, topic, e)
            return
        }

        if (!workerAnnotation.autoComplete) {
            log.debug { "Worker for topic '$topic' finished. Completion is left to the worker." }
            return
        }
        externalTaskService.complete(externalTask.id, WORKER_ID, toOutputVariables(rawWorkerResult))
        log.debug { "Worker for topic '$topic' finished and the external task was auto-completed." }
    }

    fun getWorkerByTopic(topic: String): Any =
        registeredWorkers[topic] ?: throw IllegalArgumentException("No worker registered for topic '$topic'.")

    /** Translates a worker-thrown [BpmnErrorOccurred] into an engine BPMN error; rethrows anything else. */
    private fun handleWorkerException(externalTask: LockedExternalTask, topic: String, exception: Exception) {
        val cause = exception.cause ?: exception
        if (cause !is BpmnErrorOccurred) throw cause
        externalTaskService.handleBpmnError(externalTask.id, WORKER_ID, cause.errorCode, cause.message, cause.payload)
        log.debug { "Worker for topic '$topic' raised BPMN error '${cause.errorCode}'." }
    }

    private fun findExternalTask(topic: String): LockedExternalTask {
        val matchingTasks = externalTaskService
            .fetchAndLock(1, WORKER_ID)
            .topic(topic, LOCK_DURATION_MS)
            .enableCustomObjectDeserialization()
            .execute()
        if (matchingTasks.isEmpty()) {
            val waitingTopics = externalTaskService.createExternalTaskQuery().list().joinToString(", ") { it.topicName }
            throw IllegalArgumentException("No external task waiting on topic '$topic'. Waiting topics: [$waitingTopics]")
        }
        return matchingTasks.first()
    }

    private fun findWorkerMethod(worker: Any, topic: String): Pair<KFunction<*>, ProcessEngineWorker> {
        val workerMethods = worker::class.functions
            .mapNotNull { method -> method.findAnnotation<ProcessEngineWorker>()?.let { method to it } }
            .filter { (_, annotation) -> annotation.topic == topic }
        require(workerMethods.size == 1) {
            "Expected exactly one @ProcessEngineWorker method with topic '$topic' in " +
                "'${worker::class.simpleName}', found ${workerMethods.size}."
        }
        return workerMethods.first()
    }

    private fun mapVariablesToParameters(
        worker: Any,
        method: KFunction<*>,
        externalTask: LockedExternalTask,
    ): Map<KParameter, Any?> = buildMap {
        put(method.instanceParameter!!, worker)
        method.valueParameters.forEach { parameter ->
            val strategy = getStrategyToResolveParameter(parameter)
            put(parameter, resolveValueToParameter(parameter, strategy, externalTask))
        }
    }

    private fun getStrategyToResolveParameter(parameter: KParameter): ParameterStrategy = when {
        parameter.hasAnnotation<Variable>() -> ParameterStrategy.VARIABLE
        parameter.type.jvmErasure == TaskInformation::class -> ParameterStrategy.TASK_INFORMATION
        parameter.type.jvmErasure == Map::class -> ParameterStrategy.VARIABLE_MAP
        else -> throw IllegalArgumentException("Could not find a strategy to resolve parameter '${parameter.name}'.")
    }

    private fun resolveValueToParameter(
        parameter: KParameter,
        strategy: ParameterStrategy,
        externalTask: LockedExternalTask,
    ): Any? = when (strategy) {
        ParameterStrategy.TASK_INFORMATION -> externalTask.toTaskInformation()
        ParameterStrategy.VARIABLE_MAP -> externalTask.variables
        ParameterStrategy.VARIABLE -> resolveValueForParameterWithVariableAnnotation(parameter, externalTask)
    }

    private fun resolveValueForParameterWithVariableAnnotation(
        parameter: KParameter,
        externalTask: LockedExternalTask,
    ): Any? {
        val variableName = getVariableName(parameter)
        val rawValue = externalTask.variables[variableName]
        return convertToParameterType(rawValue, parameter.type)
    }

    private fun getVariableName(parameter: KParameter): String {
        val configuredName = parameter.findAnnotation<Variable>()?.name?.takeIf { it.isNotBlank() && it != DEFAULT_UNNAMED_NAME }
        return configuredName ?: parameter.name
            ?: throw IllegalArgumentException("Cannot resolve variable name for parameter; specify @Variable(name = \"...\").")
    }

    private fun convertToParameterType(rawValue: Any?, targetType: KType): Any? {
        if (rawValue == null) return null
        val targetClass = targetType.jvmErasure.java
        return if (targetClass.isInstance(rawValue)) rawValue else objectMapper.convertValue(rawValue, targetClass)
    }

    private fun toOutputVariables(rawWorkerResult: Any?): Map<String, Any?> =
        (rawWorkerResult as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value } ?: emptyMap()

    private fun extractTaskTopicFromWorker(worker: Any): String {
        val workerAnnotations = worker::class.functions.mapNotNull { it.findAnnotation<ProcessEngineWorker>() }
        require(workerAnnotations.size == 1) {
            "Expected exactly one @ProcessEngineWorker method in '${worker::class.simpleName}', found ${workerAnnotations.size}."
        }
        return workerAnnotations.first().topic
    }

    private enum class ParameterStrategy { VARIABLE, TASK_INFORMATION, VARIABLE_MAP }

    private companion object {
        const val WORKER_ID = "test-worker"
        const val LOCK_DURATION_MS = 30_000L
    }
}
