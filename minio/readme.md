MinIO object storage service, using EFS as the storage volume, with automatic binding and scaling.

# Deploy Using YAML
```shell
kubectl apply -f minio-config.yaml
kubectl apply -f minio-statefulset.yaml
kubectl apply -f minio-service.yaml
```