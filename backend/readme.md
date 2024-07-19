Java backend, based on springboot, control the while system and respond to frontend requests.

# 1 Prepare Configuration Files
Configuration files are located in ../yamls

1. **backend-configmap.yaml**: Contains configuration information for the backend service, such as database connection strings, etc.
2. **backend-deployment.yaml**: Defines the deployment configuration for the backend service, including the image used, the number of replicas, etc.
3. **backend-service.yaml**: Defines a service to allow the backend service to be accessed by other services within the cluster or from outside the cluster.

# 2 Create ConfigMap
```bash
kubectl apply -f backend-configmap.yaml
```

# 3 Deploy Backend Service
```bash
kubectl apply -f backend-deployment.yaml
```

# 4 Create Service
```bash
kubectl apply -f backend-service.yaml
```

# 5 Verify Deployment
- Check Deployment status:

```bash
kubectl get deployments
```

- Check Pod status:

```bash
kubectl get pods
```

- Check Service status:

```bash
kubectl get services
```