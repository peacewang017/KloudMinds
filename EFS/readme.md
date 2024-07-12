# 配置 cli
```shell
helm repo add aws-efs-csi-driver https://kubernetes-sigs.github.io/aws-efs-csi-driver/
helm repo update aws-efs-csi-driver
helm upgrade --install aws-efs-csi-driver --namespace kube-system aws-efs-csi-driver/aws-efs-csi-driver
```

# 创建 EFS 并挂载
首先创建 EFS;

ssh 到**每一个** node 中，执行：
```shell
cd /
sudo mkdir efs
sudo yum install -y amazon-efs-utils

# 注意替换 EFS ID
sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport fs-06f38ed41812bdc14.efs.us-east-1.amazonaws.com:/ efs
```

将 EFS 挂载到每一个 node。

# 部署 efs-storageclass 作为默认存储来源
```shell
kubectl apply -f efs-sc.yaml
```