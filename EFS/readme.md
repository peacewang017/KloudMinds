EFS storage volume configuration, set as the default StorageClass in the Kubernetes cluster.

# 1 Configure CLI
```shell
helm repo add aws-efs-csi-driver https://kubernetes-sigs.github.io/aws-efs-csi-driver/
helm repo update aws-efs-csi-driver
helm upgrade --install aws-efs-csi-driver --namespace kube-system aws-efs-csi-driver/aws-efs-csi-driver
```

# 2 Create EFS and Mount
First, create an EFS;

SSH into each node and execute:
```shell
cd /
sudo mkdir efs
sudo yum install -y amazon-efs-utils

# 注意替换 EFS ID
sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport fs-06f38ed41812bdc14.efs.us-east-1.amazonaws.com:/ efs
```

Mount the EFS to each node.

# 3 Deploy efs-storageclass as the Default Storage Source
```shell
kubectl apply -f efs-sc.yaml
```