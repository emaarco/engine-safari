package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.BikeOrderProcess
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PaymentConfirmationServiceTest {

    private val process = mockk<BikeOrderProcess>()
    private val service = PaymentConfirmationService(process)

    @Test
    fun `confirming charged payments broadcasts the payment-charged message to the engine`() {
        every { process.broadcastPaymentCharged() } just Runs

        service.confirmChargedPayments()

        verify(exactly = 1) { process.broadcastPaymentCharged() }
    }
}
