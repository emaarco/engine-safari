package de.emaarco.example.process.jgiven

import com.tngtech.jgiven.annotation.BeforeStage
import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import de.emaarco.example.adapter.inbound.cibseven.AbortRegistrationDelegate
import de.emaarco.example.adapter.inbound.cibseven.SendConfirmationMailDelegate
import de.emaarco.example.adapter.inbound.cibseven.SendWelcomeMailDelegate
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Errors
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import org.cibseven.community.bpm.extension.jgiven.JGivenProcessStage
import org.cibseven.community.bpm.extension.jgiven.ProcessStage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.toolisticon.testing.jgiven.step
import org.cibseven.bpm.engine.delegate.BpmnError
import org.cibseven.bpm.engine.test.mock.Mocks

@JGivenProcessStage
open class NewsletterSubscriptionActionStage :
    ProcessStage<NewsletterSubscriptionActionStage, NewsletterProcessBean>() {

    @ScenarioState
    val sendConfirmationMailUseCase = mockk<SendConfirmationMailUseCase>()

    @ScenarioState
    val sendWelcomeMailUseCase = mockk<SendWelcomeMailUseCase>()

    @ScenarioState
    val abortSubscriptionUseCase = mockk<AbortSubscriptionUseCase>()

    @BeforeStage
    fun registerDelegates() {
        Mocks.reset()
        Mocks.register("sendConfirmationMailDelegate", SendConfirmationMailDelegate(sendConfirmationMailUseCase))
        Mocks.register("sendWelcomeMailDelegate", SendWelcomeMailDelegate(sendWelcomeMailUseCase))
        Mocks.register("abortRegistrationDelegate", AbortRegistrationDelegate(abortSubscriptionUseCase))
    }

    fun the_confirmation_mail_is_sent_successfully() = step {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
    }

    fun the_confirmation_mail_fails_with_an_invalid_mail_error() = step {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } throws
            BpmnError(Errors.ERROR_INVALID_MAIL.code)
    }

    fun the_welcome_mail_is_sent_successfully() = step {
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs
    }

    fun the_subscription_can_be_aborted() = step {
        every { abortSubscriptionUseCase.abort(any()) } just Runs
    }

    fun the_form_is_submitted_for(@Quoted subscriptionId: String) = step {
        processInstanceSupplier = NewsletterProcessBean(camunda)
        processInstanceSupplier.startBySubmittingForm(subscriptionId)
        continueToNextWaitState()
    }

    fun the_subscription_is_confirmed_for(@Quoted subscriptionId: String) = step {
        camunda.runtimeService.createMessageCorrelation(Messages.MESSAGE_SUBSCRIPTION_CONFIRMED.value)
            .processInstanceVariableEquals("subscriptionId", subscriptionId)
            .correlate()
        continueToNextWaitState()
    }

    fun the_reminder_timer_fires() = step {
        fireTimer(Elements.TIMER_EVERY_DAY.value)
        continueToNextWaitState()
    }

    fun the_abort_timer_fires() = step {
        fireTimer(Elements.TIMER_AFTER_3_DAYS.value)
        continueToNextWaitState()
    }

    /**
     * Fires the timer job of the given boundary event directly, regardless of its due date — we
     * verify that the timer path is wired, not the real waiting duration.
     */
    private fun fireTimer(timerActivityId: String) {
        val timer = camunda.managementService.createJobQuery()
            .timers().activityId(timerActivityId).singleResult()
        requireNotNull(timer) { "no timer job found for activity '$timerActivityId'" }
        camunda.managementService.executeJob(timer.id)
    }

    /**
     * Drives the process across every parked `camunda:asyncAfter` continuation until it reaches its
     * next wait state. The job executor is disabled in tests for determinism, so these message jobs
     * would otherwise never run and the process would stay stuck right after the previous step.
     */
    private fun continueToNextWaitState(maxIterations: Int = 50) {
        repeat(maxIterations) {
            val job = camunda.managementService.createJobQuery()
                .active().messages().listPage(0, 1).firstOrNull() ?: return
            camunda.managementService.executeJob(job.id)
        }
    }
}
