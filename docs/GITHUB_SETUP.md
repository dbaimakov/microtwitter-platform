# GitHub Setup Guide

This repository is already organized as a portfolio-friendly monorepo. The cleanest publishing approach is to create one public repository that showcases the full platform, then use the README and Docker deployment assets to make onboarding easy for recruiters, instructors, and collaborators.

## Recommended Repository Metadata

**Repository name**

`microtwitter-platform`

Alternative names:

- `microtwitter-microservices-platform`
- `microtwitter-oauth-microservices`
- `spring-microtwitter-platform`

**Repository description**

`Spring Boot microservices platform with user management, subscriptions, messaging, timeline aggregation, GitHub OAuth 2.0, JWT, MySQL, Docker Compose, and GitHub Actions.`

**Suggested topics**

- `spring-boot`
- `java`
- `microservices`
- `oauth2`
- `jwt`
- `mysql`
- `docker`
- `docker-compose`
- `webflux`
- `rest-api`
- `portfolio-project`

## Best Publishing Strategy

Use a **single monorepo** rather than separate repositories. That gives you:

- one polished landing page and architecture story
- one deployment command for the complete platform
- one CI workflow
- cleaner navigation for instructors and employers
- room to show the original local-auth milestone and the OAuth-enhanced milestone side by side

## Command-Line Upload

From the repository root:

```bash
git init
git add .
git commit -m "feat: initial MicroTwitter platform monorepo"
git branch -M main
```

Create the GitHub repository using GitHub CLI:

```bash
gh repo create microtwitter-platform --public --source=. --remote=origin --push
```

If you prefer to create the repository in the browser first, use:

```bash
git remote add origin https://github.com/<your-username>/microtwitter-platform.git
git push -u origin main
```

## Recommended Repository Settings

After the first push:

1. Enable GitHub Actions.
2. Add a repository description and topics.
3. Pin the repository on your profile.
4. Protect the `main` branch.
5. Add repository secrets if you plan to use GitHub-hosted deployments later.

## Useful Secrets

If you later publish Docker images or deploy automatically, add secrets such as:

- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `JWT_SECRET`
- container registry credentials for your chosen runtime

## Portfolio Polish Checklist

- keep the root README as the main landing page
- keep screenshots or animated demos in a future `docs/assets/` folder
- use GitHub Releases for milestone snapshots
- add a short architecture image to the repo social preview later
- leave the CI workflow enabled so visitors can see build status

## Suggested First Commits

A clean public history looks better than one giant dump. If you want to refine the history before publishing, use a sequence like:

1. `chore: scaffold monorepo structure`
2. `feat: add docker compose deployment for all services`
3. `docs: add enterprise README and deployment guide`
4. `ci: add GitHub Actions build workflow`

If you do not want to rewrite history, one strong initial commit is completely acceptable.
