# 🏛️ Camunda 7 - Classic Implementation

Classic **Camunda 7** embedded engine implementation using the traditional JavaDelegate pattern.

## About

Camunda 7 is the mature, widely-adopted embedded BPMN workflow engine that's reaching end of life. 
This implementation showcases the traditional approach with delegates and direct engine API interaction.

## Why This Module?

This is the baseline. The "before" snapshot. 
If you're migrating from Camunda 7, this represents where you likely are today. 
Use it to compare with the alternatives (CIB7, Operaton) or the abstracted approach (Process-Engine-API).

## Quick Start

1. Start PostgreSQL: `docker-compose -f stack/docker-compose.yml up -d`
2. Use the IntelliJ run configuration in `/run` folder
3. Access Camunda UI: http://localhost:8081/camunda (admin/admin)
4. API operations available in `/bruno` folder

## History Cleanup

Automatic history cleanup is configured in `HistoryCleanupConfiguration.kt`:

| Setting | Value | Description |
|---|---|---|
| `historyCleanupStrategy` | `removalTimeBased` | Removes data based on removal time (end time + TTL) |
| `historyCleanupBatchWindowStartTime` | `22:00` | Cleanup window opens at 10 PM |
| `historyCleanupBatchWindowEndTime` | `06:00` | Cleanup window closes at 6 AM |
| `historyCleanupBatchSize` | `500` | Instances removed per transaction |
| `historyCleanupDegreeOfParallelism` | `1` | Parallel cleanup threads |

The `historyTimeToLive` is defined per process in `newsletter.bpmn` (`P5D`).

### Manual Trigger

```
POST http://localhost:8081/camunda/api/engine/default/history/cleanup
```
