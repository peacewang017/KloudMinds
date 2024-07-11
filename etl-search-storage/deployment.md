```bash
docker build -t 1098822169/etl-service:latest .
docker push 1098822169/etl-service:latest
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```