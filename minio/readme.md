# 前置条件
确保 EFS/readme.md 中的 3 个操作（配置 cli + 创建并挂在 EFS + 部署 storage-class）已经完成

# 使用 yaml 部署
```shell
kubectl apply -f minio-config.yaml
kubectl apply -f minio-statefulset.yaml
kubectl apply -f minio-service.yaml
```