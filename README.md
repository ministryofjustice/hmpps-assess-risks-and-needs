# Assess Risks and Needs Service

[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

A Spring Boot app to get Risk and Need for offenders across HMPPS.

[Swagger API documentation is available](https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/swagger-ui.html)

## Dependencies
* Java JDK 21
* An editor/IDE
* Gradle
* Docker
* Postgres
* HMPPS Auth


### Start the application

```bash
make up
``` 

Go to http://localhost:8080/health

### Start the application in Dev mode

```bash
make dev-up
```

This allows you to run the linter and tests with:

```bash
make lint-fix
```

```bash
make test
```

For a full list of commands run
```bash
make
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

### Health

- `/health`: provides information about the application health and its dependencies.  This should only be used  
  by offender assessment service health monitoring (e.g. pager duty) and not other systems who wish to find out the   
  state of offender assessment service.
- `/info`: provides information about the version of deployed application.  

## RSR Predictor implementation  

The service contains implementations for the OASys and Data Science (ONNX) RSR calculators.
Only one implementation can be used at once, selected using the spring profiles
- `oasys-rsr` to use the OASys calculator
- `onnx-rsr` to use the ONNX implementation

It is not possible to store the ONNX files in this public repository as it has not been approved for release. Sample static and dynamic files which return constant values is provided to facilitate unit tests.
A test ONNX file is also used in the application yaml to enable spring boot startup on local machine when using 'onnx-rsr' profile. See onnx-path in the yaml snippet below.

```
onnx-predictors:
  onnx-path: classpath:/onnx/rsr_v0.0.0_const_brief.onnx
  offence-codes-path: classpath:/onnx/offence_codes_v0.0.0.json
```
