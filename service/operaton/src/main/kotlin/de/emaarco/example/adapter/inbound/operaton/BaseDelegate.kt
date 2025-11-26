package de.emaarco.example.adapter.inbound.operaton

import org.operaton.bpm.engine.delegate.DelegateExecution
import org.operaton.bpm.engine.delegate.JavaDelegate
import mu.KotlinLogging

abstract class BaseDelegate : JavaDelegate {
    protected val log = KotlinLogging.logger {}
    
    override fun execute(execution: DelegateExecution) {
        try {
            executeTask(execution)
        } catch (e: Exception) {
            log.error(e) { "Error while processing Operaton task" }
            throw e
        }
    }
    
    abstract fun executeTask(execution: DelegateExecution)
} 