# 🏛️ CIB7 with Process-Engine-API

This module demonstrates **CIB7** integration using the **process-engine-api** framework.
The framework is an engine-neutral abstraction layer for process orchestration.

## About This Module

This module is a clone of the Camunda 7 with Process-Engine-API module, adapted to use CIB7
as the underlying process engine through the process-engine-api CIB7 adapter.

## About CIB7

CIB7 (Community-in-a-Box 7) is a community-maintained distribution of Camunda Platform 7,
providing compatibility with Camunda 7 APIs while being open-source and community-driven.

Visit [cibseven.org](https://cibseven.org/) for more information.

## About Process-Engine-API

[Process-Engine-API](https://github.com/bpm-crafters/process-engine-api) is a framework that
provides **engine neutrality** for BPMN process engines, similar to how Spring Cloud Stream
abstracts messaging systems or JPA abstracts databases.

### Key Benefits

- **Engine Neutrality**: Write code once, switch engines without changing business logic
- **Adapter Pattern**: Provides adapters for different engines (Camunda 7, CIB7, Camunda 8, etc.)
- **Worker-Based**: Uses worker pattern instead of engine-specific implementations
- **Abstraction Layer**: Shields your domain code from engine-specific APIs

## Implementation Approach

- **Engine**: CIB7 via process-engine-api adapter
- **Service Tasks**: Worker-based with scheduled delivery (no direct JavaDelegate coupling)
- **Process Deployment**: Automatic via process-engine-api worker
- **Task Execution**: Scheduled polling with configurable intervals (5 seconds)
- **Process Model Generation**: Automatic BPMN-to-Kotlin code generation on build

## Configuration

- **Port**: 8081
- **Database**: PostgreSQL (engine-safari)
- **Admin User**: admin/admin
- **Webapp Path**: /camunda
- **CIB7 Adapter**: Uses process-engine-adapter-cib-seven-embedded from Maven Local

## Why Use Process-Engine-API with CIB7?

If you're considering different process engines or planning migrations, using process-engine-api with CIB7 means:
- Your domain logic stays engine-neutral
- Easy switching between CIB7, Camunda 7, Camunda 8, or Operaton
- Only configuration changes needed
- No rewriting of business logic
- Cleaner architecture with better separation of concerns

## Technical Details

The implementation uses the same domain models, application services, and business logic as the
Camunda 7 module. The only differences are:
- Dependency: Uses CIB7 adapter instead of C7 adapter
- Import statements: `org.cibseven.bpm.engine.*` instead of `org.camunda.bpm.engine.*`
- Configuration: CIB7-specific settings (JWT authentication for webclient)

This demonstrates the power of the process-engine-api abstraction - minimal code changes to switch engines!
