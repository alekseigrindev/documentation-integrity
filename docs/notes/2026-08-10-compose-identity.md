# Compose Identity Is Part of Reproducibility

Date: 2026-08-10

## Context

Docker Compose derived the project name `infra` from the configuration
directory. A separate project using the same directory name could therefore address the
predecessor project's networks and volumes instead of creating an isolated
environment.

## Decision

Pin `documentation-integrity` as the Compose project name. Do not rely on
developers remembering the `--project-name` flag. Remove the empty database
initialization bind mount and leave schema ownership to Flyway.

## Evidence

The rendered configuration names the network
`documentation-integrity_default` and the volume
`documentation-integrity-postgres-data`. PostgreSQL reached healthy status and
Kafka started under the isolated project identity.

## Trade-off

An explicit name prevents cross-project state reuse caused by directory-derived
naming, but concurrent copies of this project still require an explicit
project-name override.

## Article Angle

Hidden Compose identity: how identical directory layouts can couple supposedly
independent local environments.