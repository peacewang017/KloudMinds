The parsing module written in Java, used for parsing pdf/docx/doc files and interacting with elastic-search.

# 1 Create Secrets
```bash
kubectl apply -f jiexi-secrets.yaml
```

# 2 Create ConfigMap
```bash
kubectl apply -f jiexi-configMap.yaml
```

# 3 Deploy the Service
```bash
kubectl apply -f jiexi-deployment.yaml
```

# 4 Verify the Deployment
- Check the status of Secrets:

```bash
kubectl get secrets
```

- Check the status of ConfigMap:

```bash
kubectl get configmaps
```

- Check the status of Deployment:

```bash
kubectl get deployments
```

- Check the status of Pods:

```bash
kubectl get pods
```

Ensure all Pods are in the Running state and the configuration is correctly applied.