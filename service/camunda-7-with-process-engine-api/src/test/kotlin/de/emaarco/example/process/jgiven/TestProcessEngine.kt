package de.emaarco.example.process.jgiven

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.history.HistoryLevel
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension

/**
 * Builds the standalone in-memory process engine for the JGiven tests programmatically, replacing
 * the classpath `camunda.cfg.xml` that `ProcessEngineExtension.builder()` would otherwise load.
 * Keeps the engine configuration in code, next to the tests that rely on it.
 */
object TestProcessEngine {

    fun extension(): ProcessEngineExtension =
        ProcessEngineExtension.builder().useProcessEngine(engine()).build()

    private fun engine(): ProcessEngine =
        StandaloneInMemProcessEngineConfiguration().apply {
            processEngineName = "jgiven-test"
            isJobExecutorActivate = false
            historyLevel = HistoryLevel.HISTORY_LEVEL_FULL
            jdbcUrl = "jdbc:h2:mem:camunda-7-pea-jgiven;DB_CLOSE_DELAY=-1"
        }.buildProcessEngine()
}
