# 1 Build Docker Image
Run the following command in the project root directory to build the Docker image:

```sh
docker build -t frontend .
```

### 2 Push Image to Container Registry

Push the built image to Docker Hub or another container image registry:

```sh
docker tag frontend 1098822169/frontend:latest
docker push 1098822169/frontend:latest
```

### 3 Create Kubernetes Deployment Configuration

Create a deployment.yaml file to define the Kubernetes deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: 1098822169/frontend:latest
        ports:
        - containerPort: 80
```

### 4 Deploy to Kubernetes
```shell
kubectl apply -f frontend-deployment.yaml
kubectl apply -f frontend-service.yaml
```