# Plugin Health

## Introduction

Lutece Health plugin based on the [MicroProfile Health](https://microprofile.io/specifications/microprofile-health/) specification.

Provides CDI-based health checks discovered automatically by the MicroProfile Health runtime (OpenLiberty `mpHealth` feature).

## Health Checks

| Check | Annotation | Endpoint | Description |
|-------|-----------|----------|-------------|
| `DatabaseReadinessCheck` | `@Readiness` | `/health/ready` | Validates database connectivity before accepting traffic |
| `DatabaseLivenessCheck` | `@Liveness` | `/health/live` | Validates database connectivity at runtime |
| `WebappStartupCheck` | `@Startup` | `/health/started` | Checks `AppInit.isWebappSuccessfullyLoaded()` once at boot |

## Configuration

### Database check

The validation query and timeout are configurable via MicroProfile Config (`@ConfigProperty`):

| Property | Default | Description |
|----------|---------|-------------|
| `portal.checkvalidconnectionsql` | `SELECT 1` | SQL validation query |
| `healthcheck.database.timeoutMs` | `5000` | Query timeout in milliseconds |

## Endpoints

The MicroProfile Health runtime exposes the following endpoints automatically:

| Endpoint | Description |
|----------|-------------|
| `/health` | All checks (readiness + liveness + startup) |
| `/health/ready` | `@Readiness` checks only |
| `/health/live` | `@Liveness` checks only |
| `/health/started` | `@Startup` checks only |

### Response example

```json
{
  "status": "UP",
  "checks": [
    {
      "name": "database-readiness",
      "status": "UP",
      "data": {
        "responseTimeMs": 3,
        "validationQuery": "SELECT 1"
      }
    },
    {
      "name": "webapp",
      "status": "UP"
    }
  ]
}
```

## Installation

Add the dependency to your Lutece site `pom.xml`:

```xml
<dependency>
    <groupId>fr.paris.lutece.plugins</groupId>
    <artifactId>plugin-health</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <type>lutece-plugin</type>
</dependency>
```

Requires:
- Lutece Core 8.0.0+
- OpenLiberty with `mpHealth` feature enabled

## Project Structure

```
src/java/fr/paris/lutece/plugins/health/
  check/
    AbstractDatabaseCheck.java      -- Shared DB check logic with timeout
    DatabaseReadinessCheck.java     -- @Readiness
    DatabaseLivenessCheck.java      -- @Liveness
    WebappStartupCheck.java         -- @Startup
```
