要将一个Vue前端应用容器化并部署到Kubernetes (k8s) 的Pod上，你需要遵循以下步骤：

### 1. 创建Dockerfile

首先，在你的Vue项目根目录（与[`package.json`]同级）创建一个 `Dockerfile`。这个文件将指导Docker如何构建你的Vue应用的镜像。以下是一个基本的 `Dockerfile`示例：

```Dockerfile
# 基于Node.js官方镜像构建
FROM node:lts-alpine as build-stage

# 设置工作目录
WORKDIR /app

# 复制package.json和package-lock.json文件
COPY package*.json ./

# 安装项目依赖
RUN npm install

# 复制项目文件
COPY . .

# 构建应用
RUN npm run build

# 生产阶段使用nginx
FROM nginx:stable-alpine as production-stage

# 从构建阶段复制构建结果到nginx目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 暴露80端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
```

### 2. 构建Docker镜像

在项目根目录下，运行以下命令来构建Docker镜像：

```sh
docker build -t frontend .
```

### 3. 推送镜像到容器仓库

将构建好的镜像推送到Docker Hub或其他容器镜像仓库中：

```sh
docker tag frontend 1098822169/frontend:latest
docker push 1098822169/frontend:latest
```

### 4. 创建Kubernetes部署配置

创建一个 `deployment.yaml`文件，用于定义Kubernetes部署：

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

### 5. 部署到Kubernetes

确保你已经配置了对Kubernetes集群的访问。然后，运行以下命令来部署你的应用：

```sh
kubectl apply -f frontend-deployment.yaml
```

### 6. 创建Service以暴露应用

创建一个 `service.yaml`文件，以便外部访问你的应用：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
spec:
  type: NodePort
  ports:
  - port: 80
    nodePort: 30001
  selector:
    app: frontend
```

然后，运行以下命令来创建Service：

```sh
kubectl apply -f frontend-service.yaml
```

完成以上步骤后，你的Vue应用将被容器化并部署到Kubernetes Pod上，外部可以通过分配给Service的IP地址访问应用。

```sh
kubectl get nodes -o wide
```
