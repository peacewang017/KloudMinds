Kubernetes cluster monitoring tools, configuring Prometheus, Grafana, and ELK Stack in the KloudVault project for monitoring and log management.

# 1 Check Kubectl Context

```bash
kubectl config current-context
```

## 1.1 Install Prometheus Operator

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install prometheus-operator prometheus-community/kube-prometheus-stack
kubectl --namespace default get pods -l "release=prometheus-operator"
```

### 1.1.1 Prometheus

```bash
kubectl port-forward svc/prometheus-operator-kube-p-prometheus 9090:9090 -n default
```

### 1.1.2 Alertmanager

```bash
kubectl port-forward svc/prometheus-operator-kube-p-alertmanager 9093:9093 -n default
```

### 1.1.3 Grafana
Access Grafana with the following command:

```bash
kubectl get secret --namespace default prometheus-operator-grafana -o jsonpath="{.data.admin-password}"
# go https://www.base64decode.org/ to decode the password
# admin
# prom-operator
kubectl port-forward svc/prometheus-operator-grafana 3000:80 -n default 
# go http://localhost:3000/ to access Grafana
```

