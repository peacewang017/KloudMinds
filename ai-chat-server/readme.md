The AI chat server written in Python, based on Flask and OpenAI API.

# 1 Build Docker Image
First, ensure you are logged in to your container registry. For example, if using Docker Hub:

```sh
docker login
```

Build the Docker image and push it to the registry:

```sh
docker build -t 1098822169/ai-chat-server:latest .
docker push 1098822169/ai-chat-server:latest
```

# 2 Deploy the Application
```sh
kubectl apply -f ai-chat-deployment.yaml
kubectl apply -f ai-chat-service.yaml
```


# 3 Verify the Deployment
Check the status of Pods and Services:

```sh
kubectl get pods
kubectl get services
```


