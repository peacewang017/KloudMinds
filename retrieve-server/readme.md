Python-based vector search service, which handles requests and performs upload/download/search operations in a vector database, based on Flask and Weaviate API.

# 1 Containerization and Deployment

## 1.1 Build and Push Docker Image
```sh
docker login
```

Build the Docker image and push it to the registry:

```sh
docker build -t 1098822169/retrieve-server:latest .
docker push 1098822169/retrieve-server:latest
```


## 1.2 Deploy the Application

```sh
kubectl apply -f retrieve-deployment.yaml
kubectl apply -f retrieve-service.yaml
```

## 1.3 Verify Deployment
Check the status of Pods and Services:

```sh
kubectl get pods
kubectl get services
```

