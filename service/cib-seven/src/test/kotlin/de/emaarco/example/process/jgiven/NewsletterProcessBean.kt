package de.emaarco.example.process.jgiven

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi
import org.cibseven.bpm.engine.ProcessEngine
import org.cibseven.bpm.engine.runtime.ProcessInstance
import java.util.function.Supplier

class NewsletterProcessBean(
    private val processEngine: ProcessEngine,
) : Supplier<ProcessInstance> {

    companion object {
        const val RESOURCE = "bpmn/newsletter.bpmn"
    }

    lateinit var processInstance: ProcessInstance

    override fun get(): ProcessInstance = processInstance

    fun startBySubmittingForm(subscriptionId: String) {
        processInstance = processEngine.runtimeService.startProcessInstanceByMessage(
            NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED.value,
            mapOf("subscriptionId" to subscriptionId),
        )
    }
}
