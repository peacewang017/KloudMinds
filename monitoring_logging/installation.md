https://chatgpt.com/share/7a684605-7786-41e1-8709-b1b5f18bd535

## Check Kubectl Context First

```bash
kubectl config current-context
```

### Prometheus Operator

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install prometheus-operator prometheus-community/kube-prometheus-stack
kubectl get pods -n default
```

### Grafana

```bash
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm install grafana grafana/grafana
kubectl get pods -n default
```

![1720599854497](image/installation/1720599854497.png)

访问 Grafana 服务：

```bash
kubectl get secret --namespace default grafana -o jsonpath="{.data.admin-password}"
# go https://www.base64decode.org/ to decode the password
export POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/name=grafana,app.kubernetes.io/instance=grafana" -o jsonpath="{.items[0].metadata.name}")
kubectl --namespace default port-forward $POD_NAME 3000
# go http://localhost:3000/ to access Grafana
```
![1720600459666](image/installation/1720600459666.png)