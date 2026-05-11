package de.emaarco.example.process

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Errors
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.PROCESS_ID
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import de.emaarco.example.application.port.outbound.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat as assertJ
import org.operaton.bpm.engine.ManagementService
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.delegate.BpmnError
import org.operaton.bpm.engine.impl.util.ClockUtil
import org.operaton.bpm.engine.runtime.ProcessInstance
import org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat
import org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.init
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.util.Date
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class NewsletterSubscriptionProcessTest {

    @Autowired
    private lateinit var processPort: NewsletterSubscriptionProcess

    @Autowired
    private lateinit var runtimeService: RuntimeService

    @Autowired
    private lateinit var managementService: ManagementService

    @Autowired
    private lateinit var processEngine: ProcessEngine

    @MockkBean
    private lateinit var sendConfirmationMailUseCase: SendConfirmationMailUseCase

    @MockkBean
    private lateinit var sendWelcomeMailUseCase: SendWelcomeMailUseCase

    @MockkBean
    private lateinit var abortSubscriptionUseCase: AbortSubscriptionUseCase

    @BeforeEach
    fun setUp() {
        init(processEngine)
    }

    @AfterEach
    fun tearDown() {
        ClockUtil.reset()
    }

    @Test
    fun `happy path - subscription is confirmed and welcome mail is sent`() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs

        val id = SubscriptionId(UUID.randomUUID())
        processPort.submitForm(id)
        val instance = processInstanceFor(id)
        drainJobs()

        assertThat(instance).isWaitingAt(Elements.ACTIVITY_CONFIRM_REGISTRATION.value)

        processPort.confirmSubscription(id)
        drainJobs()

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.START_EVENT_REQUEST_RECEIVED.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
            )
            .hasPassed(Elements.SUB_PROCESS_CONFIRMATION.value)
            .hasNotPassed(
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
                Elements.ERROR_EVENT_INVALID_MAIL.value,
                Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value,
            )
        verify(exactly = 1) { sendConfirmationMailUseCase.sendConfirmationMail(id) }
        verify(exactly = 1) { sendWelcomeMailUseCase.sendWelcomeMail(id) }
    }

    @Test
    fun `abort after 3 days - timer interrupts subprocess and aborts registration`() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { abortSubscriptionUseCase.abort(any()) } just Runs

        val id = SubscriptionId(UUID.randomUUID())
        processPort.submitForm(id)
        val instance = processInstanceFor(id)
        drainJobs()

        assertThat(instance).isWaitingAt(Elements.ACTIVITY_CONFIRM_REGISTRATION.value)

        advanceTimeBy(Duration.ofDays(3))
        fireDueTimers()
        drainJobs()

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.START_EVENT_REQUEST_RECEIVED.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.TIMER_AFTER_3_DAYS.value,
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
            )
            .hasPassed(Elements.SUB_PROCESS_CONFIRMATION.value)
            .hasNotPassed(
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
            )
        verify(exactly = 1) { abortSubscriptionUseCase.abort(id) }
        verify(exactly = 0) { sendWelcomeMailUseCase.sendWelcomeMail(any()) }
    }

    @Test
    fun `invalid mail error - process ends at 'registration not possible'`() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } throws
            BpmnError(Errors.ERROR_INVALID_MAIL.code)

        val id = SubscriptionId(UUID.randomUUID())
        processPort.submitForm(id)
        val instance = processInstanceFor(id)
        drainJobs()

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.START_EVENT_REQUEST_RECEIVED.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ERROR_EVENT_INVALID_MAIL.value,
                Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value,
            )
            .hasPassed(Elements.SUB_PROCESS_CONFIRMATION.value)
            .hasNotPassed(
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
            )
        verify(exactly = 0) { sendWelcomeMailUseCase.sendWelcomeMail(any()) }
        verify(exactly = 0) { abortSubscriptionUseCase.abort(any()) }
    }

    @Test
    fun `reminder resend - non-interrupting daily timer resends confirmation mail`() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs

        val id = SubscriptionId(UUID.randomUUID())
        processPort.submitForm(id)
        val instance = processInstanceFor(id)
        drainJobs()

        assertThat(instance).isWaitingAt(Elements.ACTIVITY_CONFIRM_REGISTRATION.value)
        verify(exactly = 1) { sendConfirmationMailUseCase.sendConfirmationMail(id) }

        advanceTimeBy(Duration.ofMinutes(2))
        fireDueTimers()
        drainJobs()

        assertThat(instance).isWaitingAt(Elements.ACTIVITY_CONFIRM_REGISTRATION.value)
        verify(exactly = 2) { sendConfirmationMailUseCase.sendConfirmationMail(id) }

        processPort.confirmSubscription(id)
        drainJobs()

        assertThat(instance)
            .isEnded
            .hasPassedInOrder(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.START_EVENT_REQUEST_RECEIVED.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.TIMER_EVERY_DAY.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
            )
            .hasPassed(Elements.SUB_PROCESS_CONFIRMATION.value)
            .hasNotPassed(
                Elements.ERROR_EVENT_INVALID_MAIL.value,
                Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value,
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
            )
    }

    private fun processInstanceFor(id: SubscriptionId): ProcessInstance {
        val instance = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(PROCESS_ID.value)
            .variableValueEquals("subscriptionId", id.value.toString())
            .singleResult()
        assertJ(instance).`as`("process instance for subscription %s", id.value).isNotNull
        return instance
    }

    private fun drainJobs(maxIterations: Int = 50) {
        repeat(maxIterations) {
            val job = managementService.createJobQuery()
                .active()
                .messages()
                .listPage(0, 1)
                .firstOrNull() ?: return
            managementService.executeJob(job.id)
        }
    }

    private fun advanceTimeBy(duration: Duration) {
        val now = ClockUtil.getCurrentTime() ?: Date()
        ClockUtil.setCurrentTime(Date(now.time + duration.toMillis()))
    }

    private fun fireDueTimers(maxIterations: Int = 20) {
        repeat(maxIterations) {
            val timer = managementService.createJobQuery()
                .timers()
                .duedateLowerThan(ClockUtil.getCurrentTime())
                .listPage(0, 1)
                .firstOrNull() ?: return
            managementService.executeJob(timer.id)
        }
    }
}
