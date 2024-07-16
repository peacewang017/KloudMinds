# 1 retrive-server 接口
retrive-server 接受文章信息和元数据，进行向量化之后存储在数据库中，对 GET 请求返回模糊化搜索的结果，接口如下：

## 1.1 上传文件
```json
${URL}/rag_upload
POST
{
    bucketname:
    filename:
    content:
}
```

## 1.2 删除文件
```json
${URL}/rag_delete
DELETE
{
    bucketname:
    filename:
}
```

## 1.3 模糊搜索
### 1.3.1 请求
```json
${URL}/rag_search
GET
{
    bucketname:
    targetcontent:
}
```

### 1.3.2 响应
```json
(若干个)
{
    filename:
    content:
}
```

# 2 容器化和部署


### 1. 构建并推送 Docker 镜像

首先，确保您已登录到您的容器注册表。例如，如果使用 Docker Hub：

```sh
docker login
```

构建 Docker 镜像并推送到注册表：

```sh
docker build -t 1098822169/retrieve-server:latest .
docker push 1098822169/retrieve-server:latest
```


### 2. 使用 `kubectl` 部署应用

```sh
kubectl apply -f retrieve-deployment.yaml
kubectl apply -f retrieve-service.yaml
```


### 3. 验证部署

检查 Pod 和 Service 状态：

```sh
kubectl get pods
kubectl get services
```

