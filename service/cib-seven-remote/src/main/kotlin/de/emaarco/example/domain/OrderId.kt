package de.emaarco.example.domain

/**
 * Identifies a running bike-order process instance on the remote engine.
 * We simply reuse the engine's process-instance id as the order identifier.
 */
@JvmInline
value class OrderId(val value: String)
