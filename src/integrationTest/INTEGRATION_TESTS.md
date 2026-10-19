# Integration Tests - ARNS API

### Run Tests locally

Dev environment:
```bash
make int-test-dev
```
Test environment:
```bash
make int-test-test
```

### Environment Variables

Get full kubectl commands from another member of the team
```bash
export AAP_CLIENT_ID=$(kubectl -n hmpps-arns-assessment-platform-dev get secret)
export AAP_CLIENT_SECRET=$(kubectl -n hmpps-arns-assessment-platform-dev get secret)
```

#### Adding environment variables

1. Docker compose e.g.
```
services:
  int:
    ...
    environment:
      - ARNS_TEST_BASE_URL
      - ARNS_TEST_API_CRN
```
1. Makefile e.g.
```
int-test-test:
	docker compose ${TEST_COMPOSE_FILES} run --rm --env ARNS_TEST_BASE_URL="<URL>" --env ARNS_TEST_API_CRN="<CRN>" int gradle integrationTest
```

### Running in CI

1. Setup test data in [repository variables](https://github.com/ministryofjustice/hmpps-assess-risks-and-needs/settings/variables/actions/INT_TEST_DATA)
1. Variables are passed to workflow. e.g.
```
- name: Run integration tests
  run: ./gradlew integrationTest
  env:
    ARNS_TEST_BASE_URL: ${{ fromJSON(vars.INT_TEST_DATA)[inputs.environment].url }}
    ARNS_TEST_API_CRN: ${{ fromJSON(vars.INT_TEST_DATA)[inputs.environment].crn }}
```
