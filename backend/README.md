# Nimbleways spring boot boilerplate

## Prerequisites

- In order to use this boilerplate you need to have java 17+ installed
- You need Git Bash or a unix based OS to run the configured commands
- Use the `mvn` wrapper (mvnw)

## Documentation
Please read the [backend documentation](docs/api/README.md) before starting using it.

## Setup

### Dependency Vulnerability Checker
When using this boilerplate for a new project, you will need to request a NVD API key in order to be able to run vulnerability check on dependencies :
- URL for requesting the API key: https://nvd.nist.gov/developers/request-an-api-key
- Jobs requiring the key:
  - dependency-security-update-database
  - dependency-security-check (in 2 workflow files)

NB: For onboarding projects, you can just disable/delete the jobs requiring the key.

### Renovate Dependency Upgrader
Create a token as described in [Renovate documentation](https://docs.renovatebot.com/modules/platform/github/#permissions).

## Features implemented in this boilerplate

- Docker-compose file to run a local postgres database
- Usage of liquibase to generate and apply database migrations
- Mapstruct to map DTOs with entities
- Lombok for handy annotations
- Jacoco for test coverage
- PMD for static code analysis
- Spring Security with JWT for authentication and authorization
- Dependency Check to analyze our dependencies and make sure they are not vulnerable

## How to

### Generate a migration

Assuming you created a new entity you need to run the command :
```
cd api
./db makeMigration "valid-migration-label"
```

/!\ Warning : always double-check the generated migrations to ensure their correctness

### Apply migrations
Migrations are automatically applied on app startup when it's run from the IDE or with  mvn spring-boot:run .

You can also apply them manually:
```
cd api
./db applyMigrations
```

### Access Swagger UI

Go to http://localhost:8080/api/swagger-ui.html

By default Swagger is enabled only during local development. To enable Swagger in other scenarios, set the environment variable `ENABLE_SWAGGER` to `true`.

### Access pgAdmin UI

To administer your local database, go to http://localhost:8008/

### Run tests

To run all tests
```
cd api
./mvnw verify
```

There a multiple coverage threshold configured as properties in the [pom.xml](api/pom.xml).

Useful reports to check in case of a threshold breach:
- line and branch coverage: `api/target/site/jacoco/index.html`
- mutation score and strength: `api/target/pit-reports/index.html`

### Run Spotless (code formatter)
To check formatting :
```
cd api
./mvnw spotless:check
```
To apply formatting :
```
cd api
./mvnw spotless:apply
```
#### IDE setup
If you're using IntelliJ, you can install the [Spotless plugin](https://plugins.jetbrains.com/plugin/22455-spotless-applier) and enable **"Run Spotless on Save"** in :   
`Settings → Tools → Actions on Save → Run Spotless`.

### TODO

#### Data

- Use specifications to query data
- Use Entity Graphs
