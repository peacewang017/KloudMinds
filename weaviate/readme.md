Cloud-based vector database service.

# 1 部署
Refer directly to:
https://weaviate.io/developers/weaviate/installation/kubernetes

```shell
helm repo add weaviate https://weaviate.github.io/weaviate-helm

# Create a Weaviate namespace
kubectl create namespace weaviate

# Deploy
helm upgrade --install \
  "weaviate" \
  weaviate/weaviate \
  --namespace "weaviate" \
  --values ./values.yaml
```

# 2 Check Service
```shell
kubectl get svc -n weaviate
```