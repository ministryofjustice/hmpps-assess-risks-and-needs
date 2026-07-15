# Integration Tests - ARNS API

### Run Tests

```bash
make int-test-dev
```

### Environment Variables

Get full kubectl commands from another member of the team
```bash
export AAP_UI_CLIENT_ID=$(kubectl -n hmpps-arns-assessment-platform-dev get secret)
export AAP_UI_CLIENT_SECRET=$(kubectl -n hmpps-arns-assessment-platform-dev get secret)
```
