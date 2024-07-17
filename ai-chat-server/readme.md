### 1. 构建并推送 Docker 镜像
首先，确保已登录到您的容器注册表。例如，如果使用 Docker Hub：

```sh
docker login
```

构建 Docker 镜像并推送到注册表：

```sh
docker build -t 1098822169/ai-chat-server:latest .
docker push 1098822169/ai-chat-server:latest
```

### 2. 使用 `kubectl` 部署应用
```sh
kubectl apply -f ai-chat-deployment.yaml
kubectl apply -f ai-chat-service.yaml
```


### 3. 验证部署
检查 Pod 和 Service 状态：

```sh
kubectl get pods
kubectl get services
```


