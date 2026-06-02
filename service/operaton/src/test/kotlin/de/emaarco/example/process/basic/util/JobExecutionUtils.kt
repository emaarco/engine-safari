package de.emaarco.example.process.basic.util

import org.operaton.bpm.engine.ProcessEngine

/**
 * Drives all pending async-continuation jobs synchronously until the process reaches its next
 * wait state (user/receive task, timer, or end). This is necessary because the job executor is
 * disabled in tests for determinism — `camunda:asyncAfter` otherwise parks message jobs that
 * nobody would ever execute, leaving the process stuck right after the previous step.
 */
fun ProcessEngine.continueToNextWaitState(maxIterations: Int = 50) {
    repeat(maxIterations) {
        val job = managementService.createJobQuery()
            .active()
            .messages()
            .listPage(0, 1)
            .firstOrNull() ?: return
        managementService.executeJob(job.id)
    }
}
