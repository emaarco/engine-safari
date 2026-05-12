package de.emaarco.example.process.jgiven

import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import de.emaarco.example.domain.SubscriptionId
import org.cibseven.community.bpm.extension.jgiven.JGivenProcessStage
import org.cibseven.community.bpm.extension.jgiven.ProcessStage
import io.mockk.verify
import io.toolisticon.testing.jgiven.step
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat
import java.util.UUID

@JGivenProcessStage
open class NewsletterSubscriptionThenStage :
    ProcessStage<NewsletterSubscriptionThenStage, NewsletterProcessBean>() {

    @ScenarioState
    lateinit var sendConfirmationMailUseCase: SendConfirmationMailUseCase

    @ScenarioState
    lateinit var sendWelcomeMailUseCase: SendWelcomeMailUseCase

    @ScenarioState
    lateinit var abortSubscriptionUseCase: AbortSubscriptionUseCase

    fun process_has_passed_in_order(vararg activityIds: String) = step {
        assertThat(processInstanceSupplier.get()).hasPassedInOrder(*activityIds)
    }

    fun confirmation_mail_was_sent(times: Int, @Quoted subscriptionId: String) = step {
        verify(exactly = times) {
            sendConfirmationMailUseCase.sendConfirmationMail(SubscriptionId(UUID.fromString(subscriptionId)))
        }
    }

    fun welcome_mail_was_sent(times: Int, @Quoted subscriptionId: String) = step {
        verify(exactly = times) {
            sendWelcomeMailUseCase.sendWelcomeMail(SubscriptionId(UUID.fromString(subscriptionId)))
        }
    }

    fun abort_was_invoked(times: Int, @Quoted subscriptionId: String) = step {
        verify(exactly = times) {
            abortSubscriptionUseCase.abort(SubscriptionId(UUID.fromString(subscriptionId)))
        }
    }
}
