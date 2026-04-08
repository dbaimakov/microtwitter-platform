# Deployment Guide

This repository is packaged for local and demo deployment with Docker Compose. The default stack runs the OAuth-enabled User Management Service together with the remaining MicroTwitter services. A lightweight overlay is also provided for the original local-auth variant.

## Prerequisites

- Docker Desktop, or Docker Engine with the Compose plugin
- Git
- Optional: Java 17 and Maven for local non-container execution

## Deployment Modes

| Mode | Command | Notes |
|---|---|---|
| OAuth-ready full stack | `docker compose up --build` | Default mode. Local username/password login still works. |
| Original local-auth UMS | `docker compose -f compose.yaml -f compose.local-auth.yaml up --build` | Uses the original semester UMS implementation. |

## Environment Variables

Copy the sample file and adjust values as needed:

```bash
cp .env.example .env
```

| Variable | Purpose | Default |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Shared MySQL root password used by all three databases | `root123` |
| `UMS_DB_PORT` | Host port for the UMS MySQL instance | `33061` |
| `SUBSCRIPTIONS_DB_PORT` | Host port for the Subscriptions MySQL instance | `33062` |
| `MESSAGES_DB_PORT` | Host port for the Messages MySQL instance | `33063` |
| `JWT_SECRET` | Signing key for the OAuth-enabled UMS JWT demo endpoint | placeholder value |
| `GITHUB_CLIENT_ID` | GitHub OAuth application client id | placeholder value |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth application client secret | placeholder value |
| `JAVA_OPTS` | Optional JVM tuning flags passed to every service container | empty |

## Startup

Start the default stack:

```bash
docker compose up --build -d
docker compose ps
```

Stream logs:

```bash
docker compose logs -f --tail=200
```

Stop everything:

```bash
docker compose down
```

Remove containers and database volumes for a clean reset:

```bash
docker compose down -v --remove-orphans
```

## Service Endpoints

| Service | URL | Health |
|---|---|---|
| User Management Service | `http://localhost:8081` | `http://localhost:8081/actuator/health` |
| Subscription Service | `http://localhost:8082/api/v1/subscriptions` | `http://localhost:8082/actuator/health` |
| Message Service | `http://localhost:8083/api/v1/messages` | `http://localhost:8083/actuator/health` |
| Timeline Service | `http://localhost:8084/api/v1/timeline` | `http://localhost:8084/actuator/health` |

## Seeded Accounts

| Username | Password | Roles |
|---|---|---|
| `alice` | `password123` | `PRODUCER`, `SUBSCRIBER` |
| `bob` | `password123` | `SUBSCRIBER` |
| `carol` | `password123` | `PRODUCER` |
| `admin` | `password123` | `ADMIN` |

## GitHub OAuth Configuration

Create a GitHub OAuth App with these local settings:

- Homepage URL: `http://localhost:8081`
- Authorization callback URL: `http://localhost:8081/login/oauth2/code/github`

Then populate `.env`:

```dotenv
GITHUB_CLIENT_ID=your-client-id
GITHUB_CLIENT_SECRET=your-client-secret
JWT_SECRET=a-long-random-secret
```

After startup, open:

- `http://localhost:8081`
- `http://localhost:8081/api/v1/auth/login/github`

## Local Development Without Docker

The original semester configuration is preserved in each service `application.yml`.

Expected local ports:

- UMS: `8081`
- Subscription Service: `8082`
- Message Service: `8083`
- Timeline Service: `8084`
- MySQL: `33061`, `33062`, `33063`

Start order for manual local runs:

1. MySQL databases
2. User Management Service
3. Subscription Service
4. Message Service
5. Timeline Service

## Troubleshooting

### Database schema does not refresh

Use:

```bash
docker compose down -v --remove-orphans
docker compose up --build -d
```

### OAuth login does not work

Confirm that:

- the callback URL in GitHub exactly matches `http://localhost:8081/login/oauth2/code/github`
- `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` are set in `.env`
- the service is running on port `8081`

### Inter-service calls fail

Confirm all containers are healthy and reachable:

```bash
docker compose ps
docker compose logs user-management-service subscription-service message-service timeline-service
```
