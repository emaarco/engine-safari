# 🔧 Operaton - Community-Driven Fork

**Operaton** - a community-driven fork of Camunda 7 maintained by the [Operaton project](https://operaton.org/).

## About Operaton

[Operaton](https://operaton.org/) is another open-source, community-maintained fork of Camunda 7. 
Unlike other forks, it's driven by community contributors without a single vendor behind it.

## Quick Start

1. Start PostgreSQL: `docker-compose -f stack/docker-compose.yml up -d`
2. Use the IntelliJ run configuration in `/run` folder
3. Access Operaton UI: http://localhost:8081/operaton (admin/admin)
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
POST http://localhost:8081/operaton/api/engine/default/history/cleanup
```
