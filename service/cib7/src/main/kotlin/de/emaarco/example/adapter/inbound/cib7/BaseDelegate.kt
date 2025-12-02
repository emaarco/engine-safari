package de.emaarco.example.adapter.inbound.cib7

import org.cibseven.bpm.engine.delegate.DelegateExecution
import org.cibseven.bpm.engine.delegate.JavaDelegate
import mu.KotlinLogging

abstract class BaseDelegate : JavaDelegate {
    protected val log = KotlinLogging.logger {}

    override fun execute(execution: DelegateExecution) {
        try {
            executeTask(execution)
        } catch (e: Exception) {
            log.error(e) { "Error while processing CIB7 task" }
            throw e
        }
    }

    abstract fun executeTask(execution: DelegateExecution)
}
