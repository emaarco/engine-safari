package de.emaarco.example.adapter.inbound.rest

import de.emaarco.example.application.port.inbound.DriveBikeOrderUseCase
import de.emaarco.example.domain.OrderId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bike-orders")
class BikeOrderController(private val useCase: DriveBikeOrderUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping
    fun startOrder(@RequestBody request: StartOrderRequest): ResponseEntity<StartOrderResponse> {
        log.debug { "Received REST-request to start a bike order: $request" }
        val orderId = useCase.startOrder(request.orderTotal)
        return ResponseEntity.ok(StartOrderResponse(orderId.value))
    }

    @PostMapping("/{orderId}/approve")
    fun approve(@PathVariable orderId: String): ResponseEntity<Unit> {
        log.debug { "Received REST-request to approve order: $orderId" }
        useCase.approveByManager(OrderId(orderId))
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{orderId}/prepare")
    fun prepare(@PathVariable orderId: String): ResponseEntity<Unit> {
        log.debug { "Received REST-request to complete bike preparation for order: $orderId" }
        useCase.completeBikePreparation(OrderId(orderId))
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{orderId}/report-defect")
    fun reportDefect(@PathVariable orderId: String): ResponseEntity<Unit> {
        log.debug { "Received REST-request to report a defect for order: $orderId" }
        useCase.reportDefect(OrderId(orderId))
        return ResponseEntity.noContent().build()
    }

    data class StartOrderRequest(val orderTotal: Long)

    data class StartOrderResponse(val orderId: String)
}
