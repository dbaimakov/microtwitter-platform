# Security Policy

## Reporting

If you discover a security issue in the sample code or deployment assets, please contact the maintainer directly or open a private security advisory if the repository has that feature enabled.

## Scope

The repository includes enterprise-style structure, Docker-based deployment, and CI automation for demonstration purposes. It should not be treated as production-ready without additional hardening.

Before any internet-facing deployment, at minimum:

- replace seeded users and demo credentials
- rotate all secrets and move them into a secret manager
- replace SHA-256 password hashing with BCrypt or Argon2
- add HTTPS and an API gateway or ingress
- add service-to-service authentication and authorization
- add automated tests, structured logging, metrics, and alerting
- review container, dependency, and image vulnerabilities regularly
