package de.emaarco.example.adapter.process

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.springframework.context.annotation.Configuration

/**
 * Configures the history cleanup job for the Camunda 7 engine.
 * @see <a href="https://docs.camunda.org/manual/7.24/user-guide/process-engine/history/history-cleanup/">History Cleanup</a>
 */
@Configuration
class HistoryCleanupConfiguration : ProcessEnginePlugin {

    override fun preInit(configuration: ProcessEngineConfigurationImpl) {
        configuration.historyCleanupStrategy = "removalTimeBased"
        configuration.historyCleanupBatchWindowStartTime = "22:00"
        configuration.historyCleanupBatchWindowEndTime = "06:00"
        configuration.historyCleanupBatchSize = 500
        configuration.historyCleanupDegreeOfParallelism = 1
    }

    override fun postInit(configuration: ProcessEngineConfigurationImpl) {}
    override fun postProcessEngineBuild(processEngine: ProcessEngine) {}
}
