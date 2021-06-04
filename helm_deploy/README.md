# Deployment Notes

## Pre-requisites

- Helm 3

Documentation for installing Helm can be found [here](https://helm.sh/docs/intro/quickstart/#install-helm)

## Dependencies

This chart uses the following dependencies

- generic-service
- generic-prometheus-alerts

These are maintained [by the Ministry of Justice in GitHub](https://github.com/ministryofjustice/hmpps-helm-charts/)

## Deploying this chart

This chart can deployed with to a K8s environment with the following command

```shell
helm upgrade <RELEASE_NAME> hmpps-assess-risks-and-needs -n <NAMESPACE> --values values-<ENV>.yaml
```

note we use environment specific values

## Rolling back releases

To list releases on the K8s namespace

```shell
helm -n <NAMESPACE> list
```

View the revisions for a release

```shell
helm -n <NAMESPACE> history <RELEASE_NAME>
```

To rollback to a revision of a release

```shell
helm -n <NAMESPACE> --wait rollback <RELEASE_NAME> <REVISION>
```
