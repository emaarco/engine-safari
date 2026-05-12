package de.emaarco.example.process.jgiven

import com.tngtech.jgiven.annotation.BeforeStage
import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import de.emaarco.example.adapter.inbound.cibseven.AbortRegistrationDelegate
import de.emaarco.example.adapter.inbound.cibseven.SendConfirmationMailDelegate
import de.emaarco.example.adapter.inbound.cibseven.SendWelcomeMailDelegate
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
import org.cibseven.bpm.engine.impl.util.ClockUtil
import org.cibseven.bpm.engine.test.mock.Mocks
import java.time.Duration
import java.util.Date

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
    fun setUp() {
        Mocks.reset()
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs
        every { abortSubscriptionUseCase.abort(any()) } just Runs

        Mocks.register("sendConfirmationMailDelegate", SendConfirmationMailDelegate(sendConfirmationMailUseCase))
        Mocks.register("sendWelcomeMailDelegate", SendWelcomeMailDelegate(sendWelcomeMailUseCase))
        Mocks.register("abortRegistrationDelegate", AbortRegistrationDelegate(abortSubscriptionUseCase))
    }

    fun the_form_is_submitted_for(@Quoted subscriptionId: String) = step {
        processInstanceSupplier = NewsletterProcessBean(camunda)
        processInstanceSupplier.startBySubmittingForm(subscriptionId)
        drainAsyncJobs()
    }

    fun the_subscription_is_confirmed_for(@Quoted subscriptionId: String) = step {
        camunda.runtimeService.createMessageCorrelation(Messages.MESSAGE_SUBSCRIPTION_CONFIRMED.value)
            .processInstanceVariableEquals("subscriptionId", subscriptionId)
            .correlate()
        drainAsyncJobs()
    }

    fun the_confirmation_mail_throws_an_invalid_mail_error() = step {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } throws
            BpmnError(Errors.ERROR_INVALID_MAIL.code)
    }

    fun the_clock_advances_by(duration: Duration) = step {
        val now = ClockUtil.getCurrentTime() ?: Date()
        ClockUtil.setCurrentTime(Date(now.time + duration.toMillis()))
        fireDueTimers()
        drainAsyncJobs()
    }

    private fun drainAsyncJobs(maxIterations: Int = 50) {
        repeat(maxIterations) {
            val job = camunda.managementService.createJobQuery()
                .active()
                .messages()
                .listPage(0, 1)
                .firstOrNull() ?: return
            camunda.managementService.executeJob(job.id)
        }
    }

    private fun fireDueTimers(maxIterations: Int = 20) {
        repeat(maxIterations) {
            val timer = camunda.managementService.createJobQuery()
                .timers()
                .duedateLowerThan(ClockUtil.getCurrentTime())
                .listPage(0, 1)
                .firstOrNull() ?: return
            camunda.managementService.executeJob(timer.id)
        }
    }
}
