# Plugin Health

## Introduction

Plugin Lutece de health check bas sur la sp cification [MicroProfile Health](https://microprofile.io/specifications/microprofile-health/).

Fournit des health checks CDI d couverts automatiquement par le runtime MicroProfile Health (feature `mpHealth` d'OpenLiberty).

## Health Checks

| Check | Annotation | Endpoint | Description |
|-------|-----------|----------|-------------|
| `DatabaseReadinessCheck` | `@Readiness` | `/health/ready` | V rifie la connectivit  BDD avant d'accepter du trafic |
| `DatabaseLivenessCheck` | `@Liveness` | `/health/live` | V rifie la connectivit  BDD en cours de fonctionnement |
| `WebappStartupCheck` | `@Startup` | `/health/started` | V rifie `AppInit.isWebappSuccessfullyLoaded()` au d marrage |

## Configuration

### Check base de donn es

La requ te de validation et le timeout sont configurables via MicroProfile Config (`@ConfigProperty`) :

| Propri t  | D faut | Description |
|----------|---------|-------------|
| `portal.checkvalidconnectionsql` | `SELECT 1` | Requ te SQL de validation |
| `healthcheck.database.timeoutMs` | `5000` | Timeout de la requ te en millisecondes |

## Endpoints

Le runtime MicroProfile Health expose automatiquement les endpoints suivants :

| Endpoint | Description |
|----------|-------------|
| `/health` | Tous les checks (readiness + liveness + startup) |
| `/health/ready` | Checks `@Readiness` uniquement |
| `/health/live` | Checks `@Liveness` uniquement |
| `/health/started` | Checks `@Startup` uniquement |

### Exemple de r ponse

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

Ajouter la d pendance dans le `pom.xml` du site Lutece :

```xml
<dependency>
    <groupId>fr.paris.lutece.plugins</groupId>
    <artifactId>plugin-health</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <type>lutece-plugin</type>
</dependency>
```

Pr requis :
- Lutece Core 8.0.0+
- OpenLiberty avec la feature `mpHealth` activ e

## Structure du projet

```
src/java/fr/paris/lutece/plugins/health/
  check/
    AbstractDatabaseCheck.java      -- Logique DB partag e avec timeout
    DatabaseReadinessCheck.java     -- @Readiness
    DatabaseLivenessCheck.java      -- @Liveness
    WebappStartupCheck.java         -- @Startup
```
