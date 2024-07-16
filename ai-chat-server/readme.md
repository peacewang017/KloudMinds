<<<<<<< HEAD
# 1 创建 docker 镜像

要将您的应用通过 Kubernetes 部署到云上，您需要以下步骤：

1. **创建 Docker 镜像并推送到容器注册表** （例如 Docker Hub、Google Container Registry、AWS ECR 等）。
2. **编写 Kubernetes 配置文件** （包括 Deployment 和 Service）。
3. **使用 `kubectl` 部署应用** 。

以下是详细步骤：

### 1. 构建并推送 Docker 镜像

首先，确保您已登录到您的容器注册表。例如，如果使用 Docker Hub：

<pre><div class="dark bg-gray-950 rounded-md border-[0.5px] border-token-border-medium"><div class="flex items-center relative text-token-text-secondary bg-token-main-surface-secondary px-4 py-2 text-xs font-sans justify-between rounded-t-md"><span>sh</span><div class="flex items-center"><span class="" data-state="closed"><button class="flex gap-1 items-center"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" class="icon-sm"><path fill="currentColor" fill-rule="evenodd" d="M7 5a3 3 0 0 1 3-3h9a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3h-2v2a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3v-9a3 3 0 0 1 3-3h2zm2 2h5a3 3 0 0 1 3 3v5h2a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1h-9a1 1 0 0 0-1 1zM5 9a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1v-9a1 1 0 0 0-1-1z" clip-rule="evenodd"></path></svg>复制代码</button></span></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="!whitespace-pre hljs language-sh">docker login
</code></div></div></pre>

构建 Docker 镜像并推送到注册表：

<pre><div class="dark bg-gray-950 rounded-md border-[0.5px] border-token-border-medium"><div class="flex items-center relative text-token-text-secondary bg-token-main-surface-secondary px-4 py-2 text-xs font-sans justify-between rounded-t-md"><span>sh</span><div class="flex items-center"><span class="" data-state="closed"><button class="flex gap-1 items-center"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" class="icon-sm"><path fill="currentColor" fill-rule="evenodd" d="M7 5a3 3 0 0 1 3-3h9a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3h-2v2a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3v-9a3 3 0 0 1 3-3h2zm2 2h5a3 3 0 0 1 3 3v5h2a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1h-9a1 1 0 0 0-1 1zM5 9a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1v-9a1 1 0 0 0-1-1z" clip-rule="evenodd"></path></svg>复制代码</button></span></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="!whitespace-pre hljs language-sh">docker build -t <your-dockerhub-username>/ai-chat-server:latest .
docker push <your-dockerhub-username>/ai-chat-server:latest
</code></div></div></pre>

### 2. 编写 Kubernetes 配置文件

创建一个 Kubernetes 配置文件 `deployment.yaml`：

<pre><div class="dark bg-gray-950 rounded-md border-[0.5px] border-token-border-medium"><div class="flex items-center relative text-token-text-secondary bg-token-main-surface-secondary px-4 py-2 text-xs font-sans justify-between rounded-t-md"><span>yaml</span><div class="flex items-center"><span class="" data-state="closed"><button class="flex gap-1 items-center"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" class="icon-sm"><path fill="currentColor" fill-rule="evenodd" d="M7 5a3 3 0 0 1 3-3h9a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3h-2v2a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3v-9a3 3 0 0 1 3-3h2zm2 2h5a3 3 0 0 1 3 3v5h2a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1h-9a1 1 0 0 0-1 1zM5 9a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1v-9a1 1 0 0 0-1-1z" clip-rule="evenodd"></path></svg>复制代码</button></span></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="!whitespace-pre hljs language-yaml">apiVersion: apps/v1
kind: Deployment
metadata:
  name: ai-chat-server-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ai-chat-server
  template:
    metadata:
      labels:
        app: ai-chat-server
    spec:
      containers:
      - name: ai-chat-server
        image: <your-dockerhub-username>/ai-chat-server:latest
        ports:
        - containerPort: 5000
---
apiVersion: v1
kind: Service
metadata:
  name: ai-chat-server-service
spec:
  selector:
    app: ai-chat-server
  ports:
    - protocol: TCP
      port: 80
      targetPort: 5000
  type: LoadBalancer
</code></div></div></pre>

### 3. 使用 `kubectl` 部署应用

确保您已连接到 Kubernetes 集群，并且 `kubectl` 已正确配置。然后，运行以下命令来应用配置文件：

<pre><div class="dark bg-gray-950 rounded-md border-[0.5px] border-token-border-medium"><div class="flex items-center relative text-token-text-secondary bg-token-main-surface-secondary px-4 py-2 text-xs font-sans justify-between rounded-t-md"><span>sh</span><div class="flex items-center"><span class="" data-state="closed"><button class="flex gap-1 items-center"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="none" viewBox="0 0 24 24" class="icon-sm"><path fill="currentColor" fill-rule="evenodd" d="M7 5a3 3 0 0 1 3-3h9a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3h-2v2a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3v-9a3 3 0 0 1 3-3h2zm2 2h5a3 3 0 0 1 3 3v5h2a1 1 0 0 0 1-1V5a1 1 0 0 0-1-1h-9a1 1 0 0 0-1 1zM5 9a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1v-9a1 1 0 0 0-1-1z" clip-rule="evenodd"></path></svg>复制代码</button></span></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="!whitespace-pre hljs language-sh">kubectl apply -f deployment.yaml
</code></div></div></pre>

### 4. 验证部署

检查 Pod 和 Service 状态：

```sh
kubectl get pods
kubectl get services
```
</code></div></div></pre>

### 5. 访问应用

如果使用了 `type: LoadBalancer`，您可以通过 `kubectl get services` 命令查看外部 IP 地址，然后通过该 IP 地址访问您的应用。

例如，如果外部 IP 地址为 `X.X.X.X`，则可以通过 `http://X.X.X.X` 访问您的应用。

### 总结

这些步骤可以帮助您将 Flask 应用部署到 Kubernetes 集群，并通过云提供的外部 IP 地址进行访问。根据不同的云平台，可能还需要一些额外的配置，例如创建防火墙规则以允许外部访问。

4

=======
# 1 ai-chat-server接口
接受文章内容和 prompt，接口 json 格式如下：

## 1.1 请求
```json
${URL}/request
POST
{
    content:
    prompt:
}
```

## 1.2 响应
```json
{
    ai_answer:
    status:
}
```

# 2 容器化和部署

# 2.1 创建 docker 镜像
>>>>>>> refs/remotes/origin/main
```shell
# 首先启动 docker 守护进程
sudo systemctl start docker

# 用户登录
docker login

# 创建和推送
sudo docker build -t peacewang017/ai-chat-server:latest .
sudo docker push peacewang017/ai-chat-server:latest
```

<<<<<<< HEAD
# 2 部署在集群中

...
=======
# 2.2 部署在集群中
...
>>>>>>> refs/remotes/origin/main
