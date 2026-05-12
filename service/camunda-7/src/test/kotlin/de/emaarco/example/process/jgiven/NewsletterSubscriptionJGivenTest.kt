package de.emaarco.example.process.jgiven

import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.junit5.ScenarioTest
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.util.ClockUtil
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Duration
import java.util.UUID

@Deployment(resources = [NewsletterProcessBean.RESOURCE])
internal class NewsletterSubscriptionJGivenTest : ScenarioTest<
    NewsletterSubscriptionActionStage,
    NewsletterSubscriptionActionStage,
    NewsletterSubscriptionThenStage,
    >() {

    companion object {
        @JvmField
        @RegisterExtension
        val extension: ProcessEngineExtension = ProcessEngineExtension.builder().build()
    }

    @ScenarioState
    val camunda: ProcessEngine = extension.processEngine

    @AfterEach
    fun tearDown() {
        ClockUtil.reset()
        Mocks.reset()
    }

    @Test
    fun `happy path - subscription is confirmed and welcome mail is sent`() {
        val id = UUID.randomUUID().toString()

        WHEN
            .the_form_is_submitted_for(id)
            .AND.the_subscription_is_confirmed_for(id)
        THEN
            .process_is_finished(Elements.END_EVENT_REGISTRATION_COMPLETED.value)
            .AND.process_has_passed_in_order(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
            )
            .AND.process_has_not_passed(
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
                Elements.ERROR_EVENT_INVALID_MAIL.value,
                Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value,
            )
            .AND.confirmation_mail_was_sent(times = 1, subscriptionId = id)
            .AND.welcome_mail_was_sent(times = 1, subscriptionId = id)
    }

    @Test
    fun `abort after 3 days - timer interrupts subprocess and aborts registration`() {
        val id = UUID.randomUUID().toString()

        WHEN
            .the_form_is_submitted_for(id)
            .AND.the_clock_advances_by(Duration.ofDays(3))
        THEN
            .process_is_finished(Elements.END_EVENT_REGISTRATION_ABORTED.value)
            .AND.process_has_passed_in_order(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.TIMER_AFTER_3_DAYS.value,
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
            )
            .AND.process_has_not_passed(
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
            )
            .AND.abort_was_invoked(times = 1, subscriptionId = id)
            .AND.welcome_mail_was_sent(times = 0, subscriptionId = id)
    }

    @Test
    fun `invalid mail error - process ends at 'registration not possible'`() {
        val id = UUID.randomUUID().toString()

        GIVEN
            .the_confirmation_mail_throws_an_invalid_mail_error()
        WHEN
            .the_form_is_submitted_for(id)
        THEN
            .process_is_finished(Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value)
            .AND.process_has_passed_in_order(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ERROR_EVENT_INVALID_MAIL.value,
                Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value,
            )
            .AND.process_has_not_passed(
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
            )
            .AND.welcome_mail_was_sent(times = 0, subscriptionId = id)
            .AND.abort_was_invoked(times = 0, subscriptionId = id)
    }

    @Test
    fun `reminder resend - non-interrupting daily timer resends confirmation mail`() {
        val id = UUID.randomUUID().toString()

        WHEN
            .the_form_is_submitted_for(id)
            .AND.the_clock_advances_by(Duration.ofMinutes(2))
            .AND.the_subscription_is_confirmed_for(id)
        THEN
            .process_is_finished(Elements.END_EVENT_REGISTRATION_COMPLETED.value)
            .AND.process_has_passed_in_order(
                Elements.START_EVENT_SUBMIT_REGISTRATION_FORM.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.TIMER_EVERY_DAY.value,
                Elements.ACTIVITY_SEND_CONFIRMATION_MAIL.value,
                Elements.ACTIVITY_CONFIRM_REGISTRATION.value,
                Elements.END_EVENT_SUBSCRIPTION_CONFIRMED.value,
                Elements.ACTIVITY_SEND_WELCOME_MAIL.value,
                Elements.END_EVENT_REGISTRATION_COMPLETED.value,
            )
            .AND.process_has_not_passed(
                Elements.ERROR_EVENT_INVALID_MAIL.value,
                Elements.END_EVENT_REGISTRATION_NOT_POSSIBLE.value,
                Elements.ACTIVITY_ABORT_REGISTRATION.value,
                Elements.END_EVENT_REGISTRATION_ABORTED.value,
            )
            .AND.confirmation_mail_was_sent(times = 2, subscriptionId = id)
            .AND.welcome_mail_was_sent(times = 1, subscriptionId = id)
    }
}
