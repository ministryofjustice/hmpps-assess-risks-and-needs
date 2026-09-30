# Integration Tests - ARNS API

### Run Tests

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
