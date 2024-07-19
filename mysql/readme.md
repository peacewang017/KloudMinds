MySQL service, storing user metadata.

# 1 Deploy Using YAML
```shell
kubectl apply -f mysql-configmap.yaml
kubectl apply -f mysql-statefulset.yaml
kubectl apply -f mysql-service.yaml
```

用户名：root
密码：123456

# 2 Import Raw Data
```shell
kubectl exec -it pod/mysql-0 -- sh

# 导入 cloud_order 数据库
```