{{- define "blockchain-lottery-indexer.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "blockchain-lottery-indexer.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "blockchain-lottery-indexer.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "blockchain-lottery-indexer.labels" -}}
app.kubernetes.io/name: {{ include "blockchain-lottery-indexer.name" . }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "blockchain-lottery-indexer.selectorLabels" -}}
app.kubernetes.io/name: {{ include "blockchain-lottery-indexer.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
