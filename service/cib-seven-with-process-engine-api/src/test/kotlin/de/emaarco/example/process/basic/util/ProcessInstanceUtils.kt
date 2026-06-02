package de.emaarco.example.process.util

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.PROCESS_ID
import de.emaarco.example.domain.SubscriptionId
import org.assertj.core.api.Assertions.assertThat
import org.cibseven.bpm.engine.RuntimeService
import org.cibseven.bpm.engine.runtime.ProcessInstance

/**
 * Finds the newsletter subscription process instance that carries the given subscription id.
 * Fails the test if no such instance exists.
 */
fun RuntimeService.findProcessInstance(subscriptionId: SubscriptionId): ProcessInstance {
    val instance = createProcessInstanceQuery()
        .processDefinitionKey(PROCESS_ID.value)
        .variableValueEquals("subscriptionId", subscriptionId.value.toString())
        .singleResult()
    assertThat(instance).`as`("process instance for subscription %s", subscriptionId.value).isNotNull
    return instance
}
