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

## Deployments and releases
See [ARNS Release Process](https://dsdmoj.atlassian.net/wiki/spaces/ARN/pages/6260917467/ARNS+Release+Process) for the background, release checks, feature flags and rollback.

| Trigger                                                      | Deploys to                          |
|--------------------------------------------------------------|-------------------------------------|
| Merge to `main`                                              | dev                                 |
| Run **Deploy to environment** manually (any branch)          | dev or test                         |
| Publish a **pre-release** (e.g. `v1.0.0-rc.1`)               | dev + test → preprod                |
| Publish a **release** (e.g. `v1.0.0`)                        | dev + test → preprod → prod         |

Preprod and prod need a manual approval in the release pipeline, and only change through a release.

In the release pipeline, preprod only deploys once the integration tests and e2e tests have passed against test.

### How to do a release
1. Go to **Releases → Draft a new release** and create a new `vX.Y.Z` tag targeting `main`
   (patch for fixes, minor for features, major for breaking changes).
2. Use the tag as the release title and click **Generate release notes**.
   Tick **Set as a pre-release** if it should stop at preprod.
3. Inform the Product Manager so they can raise a Change Request, and provide a link to the release notes.
4. Publish the release and follow the **Release pipeline** run in the Actions tab, approving preprod and prod when prompted.

### Rolling back
This service uses Postgres with Flyway migrations, and `helm rollback` does not undo migrations.
Only roll back if the previous version works with the current database schema; otherwise fix forward with a patch release.

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
