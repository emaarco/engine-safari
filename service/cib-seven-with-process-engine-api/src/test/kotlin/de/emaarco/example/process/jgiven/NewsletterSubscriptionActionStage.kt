package de.emaarco.example.process.jgiven

import com.tngtech.jgiven.annotation.BeforeStage
import com.tngtech.jgiven.annotation.Quoted
import com.tngtech.jgiven.annotation.ScenarioState
import de.emaarco.example.adapter.inbound.cib7.AbortRegistrationWorker
import de.emaarco.example.adapter.inbound.cib7.SendConfirmationMailWorker
import de.emaarco.example.adapter.inbound.cib7.SendWelcomeMailWorker
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Errors
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.ServiceTasks
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import dev.bpmcrafters.processengine.worker.BpmnErrorOccurred
import org.cibseven.community.bpm.extension.jgiven.JGivenProcessStage
import org.cibseven.community.bpm.extension.jgiven.ProcessStage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.toolisticon.testing.jgiven.step
import org.cibseven.bpm.engine.impl.util.ClockUtil
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

    @ScenarioState
    lateinit var workers: WorkerTestExecutor

    @BeforeStage
    fun setUp() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs
        every { abortSubscriptionUseCase.abort(any()) } just Runs

        workers = WorkerTestExecutor(camunda).apply {
            registerWorkers(
                SendConfirmationMailWorker(sendConfirmationMailUseCase),
                SendWelcomeMailWorker(sendWelcomeMailUseCase),
                AbortRegistrationWorker(abortSubscriptionUseCase),
            )
        }
    }

    fun the_form_is_submitted_for(@Quoted subscriptionId: String) = step {
        processInstanceSupplier = NewsletterProcessBean(camunda)
        processInstanceSupplier.startBySubmittingForm(subscriptionId)
        drainAsyncJobs()
        runWorkerIfPending(ServiceTasks.SEND_CONFIRMATION_MAIL)
    }

    fun the_subscription_is_confirmed_for(@Quoted subscriptionId: String) = step {
        camunda.runtimeService.createMessageCorrelation(Messages.MESSAGE_SUBSCRIPTION_CONFIRMED.value)
            .processInstanceVariableEquals("subscriptionId", subscriptionId)
            .correlate()
        drainAsyncJobs()
        runWorkerIfPending(ServiceTasks.SEND_WELCOME_MAIL)
    }

    fun the_confirmation_mail_throws_an_invalid_mail_error() = step {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } throws
            BpmnErrorOccurred("invalid mail", Errors.ERROR_INVALID_MAIL.code, emptyMap())
    }

    fun the_clock_advances_by(duration: Duration) = step {
        val now = ClockUtil.getCurrentTime() ?: Date()
        ClockUtil.setCurrentTime(Date(now.time + duration.toMillis()))
        fireDueTimers()
        drainAsyncJobs()
        runWorkerIfPending(ServiceTasks.ABORT_REGISTRATION)
        runWorkerIfPending(ServiceTasks.SEND_CONFIRMATION_MAIL)
    }

    private fun runWorkerIfPending(topic: String) {
        val pending = camunda.externalTaskService.createExternalTaskQuery()
            .topicName(topic)
            .notLocked()
            .count() > 0
        if (pending) {
            workers.executeWorker(topic)
            drainAsyncJobs()
        }
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
