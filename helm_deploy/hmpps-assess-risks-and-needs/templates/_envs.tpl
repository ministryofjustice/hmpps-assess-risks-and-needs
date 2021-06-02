{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.service.image.port }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "{{ .Values.service.env.SPRING_PROFILES_ACTIVE }}"

  - name: JAVA_OPTS
    value: "{{ .Values.service.env.JAVA_OPTS }}"

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: APPLICATIONINSIGHTS_CONNECTION_STRING
    value: "InstrumentationKey=$(APPINSIGHTS_INSTRUMENTATIONKEY)"

{{- end -}}
