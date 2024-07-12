# 直接参考 ndb-mysql github repo
https://github.com/mysql/mysql-ndb-operator?tab=readme-ov-file

https://github.com/mysql/mysql-ndb-operator/blob/main/docs/getting-started.md

# 1 使用 helm 安装并创建 namespace
```shell
helm repo add ndb-operator-repo https://mysql.github.io/mysql-ndb-operator/
helm repo update

helm install ndb-operator ndb-operator-repo/ndb-operator --namespace=ndb-operator --create-namespace
```

# 2 部署
```shell
# clone repo
git clone git@github.com:mysql/mysql-ndb-operator.git
cd mysql-ndb-operator

# 部署
kubectl apply -f docs/examples/example-ndb.yaml
```

# 3 运行时调试

## 3.1 检查 service 情况
```shell
kubectl get services -l mysql.oracle.com/v1=example-ndb
```

## 3.2 检查 pod 情况
```shell
kubectl get pods -l mysql.oracle.com/v1=example-ndb
```

## 3.3 获取 mysql 密码
（账户名为 root）

```shell
base64 -d <<< \
  $(kubectl get secret example-ndb-mysqld-root-password \
     -o jsonpath={.data.password})
```

## 3.4 远程连接到 mysql 内调试
```shell
kubectl exec -it example-ndb-mysqld-0 -- /bin/bash
```

