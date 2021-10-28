# Assess Risks and Needs Service

[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

A Spring Boot app to get Risk and Need for offenders across HMPPS.

[Swagger API documentation is available](https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/swagger-ui.html)

## Dependencies
* Java JDK 16
* An editor/IDE
* Gradle
* Docker
* Postgres
* OAuth  [(running in a container)](#oauth-security)
* Offender Assessments Api Service [Offender Assessments API](https://github.com/ministryofjustice/offender-assessments-api-kotlin)


#### OAuth security
In order to run the service locally, [HMPPS Auth Service](https://github.com/ministryofjustice/hmpps-auth/) is required. 
This can be run locally using the [docker-compose.yml](docker-compose.yml) file which will pull down the latest version.  
From the command line run:

```
 docker-compose up 
```  

### Build service and run tests

This service is built using Gradle. In order to build the project from the command line and run the tests, use:
```  
./gradlew clean build  
```  

### Start the application with H2 database

This will be the default database in the dev profile
``` 
datasource:
url: 'jdbc:h2:mem:testdb;INIT=create domain if not exists jsonb as text;Mode=PostgreSQL'
h2:
console:
enabled: true
``` 


### Start the application with Postgres database
This configuration can be changed to use a Postgres database using the spring boot profile `postgres`.

The service makes use of Postgres JSONB fields so it is advisable to run with postgres when making database changes.

```  
SPRING_PROFILES_ACTIVE=postgres 
```  

### Documentation
The generated documentation for the api can be viewed at http://localhost:8080/swagger-ui.html

## Tasks

❗️ This requires kubectl 1.19, as 1.20+ is incompatible with the live-1 cluster as of October 2021

### Manually sync prod to pre-prod

To manually trigger the production refresh job:
```
kubectl --namespace=hmpps-assess-risks-and-needs-prod \
  create job --from=cronjob.batch/db-refresh-job refresh-job
```

## Code style & formatting
./gradlew ktlintApplyToIdea addKtlintFormatGitPreCommitHook
will apply ktlint styles to intellij and also add a pre-commit hook to format all changed kotlin files.

### Health

- `/health`: provides information about the application health and its dependencies.  This should only be used  
  by offender assessment service health monitoring (e.g. pager duty) and not other systems who wish to find out the   
  state of offender assessment service.
- `/info`: provides information about the version of deployed application.  
  