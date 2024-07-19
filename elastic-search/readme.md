Deployment of elastic-search searvice, using operator.

# 1 Deployment

# 1.1 Deploy elastic-search
```shell
kubectl create -f https://download.elastic.co/downloads/eck/2.13.0/crds.yaml

kubectl apply -f https://download.elastic.co/downloads/eck/2.13.0/operator.yaml

cat <<EOF | kubectl apply -f -
apiVersion: elasticsearch.k8s.elastic.co/v1
kind: Elasticsearch
metadata:
  name: quickstart
spec:
  version: 8.14.3
  nodeSets:
  - name: default
    count: 1
    config:
      node.store.allow_mmap: false
EOF
```

At this point, the service should start normally.

# 1.2 Deploy Kibana
Kibana is the console for elastic-search.

```shell
cat <<EOF | kubectl apply -f -
apiVersion: kibana.k8s.elastic.co/v1
kind: Kibana
metadata:
  name: quickstart
spec:
  version: 8.14.3
  count: 1
  elasticsearchRef:
    name: quickstart
EOF
```

# 2 Runtime Debugging

## 2.1 Retrieve Password
(Default username is elastic)

```shell
kubectl get secret quickstart-es-elastic-user -o go-template='{{.data.elastic | base64decode}}'
```

## 2.2 2.2 Using Kibana
```shell
kubectl port-forward service/quickstart-kb-http 5601

## Access https://localhost:5601 in your browser
```

Username: elastic

Password: Retrieved in step 2.1s