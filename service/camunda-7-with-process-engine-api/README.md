# 🏛️ Camunda 7 with Process-Engine-API

This module demonstrates **Camunda 7** integration using the **process-engine-api** framework. 
The framework is an engine-neutral abstraction layer for process orchestration.

## About Process-Engine-API

[Process-Engine-API](https://github.com/bpm-crafters/process-engine-api) is a framework that provides **engine neutrality** for BPMN process engines, similar to how Spring Cloud Stream abstracts messaging systems or JPA abstracts databases.

### Key Benefits

- **Engine Neutrality**: Write code once, switch engines without changing business logic
- **Adapter Pattern**: Provides adapters for different engines (Camunda 7, Camunda 8, etc.)
- **Worker-Based**: Uses worker pattern instead of engine-specific implementations
- **Abstraction Layer**: Shields your domain code from engine-specific APIs

Think of it as a "translation layer" between your business logic and the underlying process engine.

## Implementation Approach

- **Service Tasks**: Worker-based with scheduled delivery (no direct JavaDelegate coupling)
- **Process Deployment**: Automatic via process-engine-api worker
- **Task Execution**: Scheduled polling with configurable intervals (5 seconds)
- **Process Model Generation**: Automatic BPMN-to-Kotlin code generation on build

## Why Use Process-Engine-API?

If you're planning to migrate from Camunda 7 to another engine (Camunda 8, Operaton, etc.), using process-engine-api means:
- Your domain logic stays the same
- Only configuration changes
- No rewriting of business logic
- Cleaner architecture with better separation of concerns
