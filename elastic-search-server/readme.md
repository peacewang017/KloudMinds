# 1 创建 docker 镜像
```shell
# 首先启动 docker 守护进程
sudo systemctl start docker

# 用户登录
docker login

# 创建和推送
sudo docker build -t peacewang017/elastic-search-server:latest .
sudo docker push peacewang017/elastic-search-server:latest
```

# 2 部署在集群中
...