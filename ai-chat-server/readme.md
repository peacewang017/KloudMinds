# 1 ai-chat-server接口
接受文章内容和 prompt，接口 json 格式如下：

## 1.1 请求
```json
${URL}/request
POST
{
    content:
    prompt:
}
```

## 1.2 响应
```json
{
    ai_answer:
    status:
}
```

# 2 容器化和部署

# 2.1 创建 docker 镜像
```shell
# 首先启动 docker 守护进程
sudo systemctl start docker

# 用户登录
docker login

# 创建和推送
sudo docker build -t peacewang017/ai-chat-server:latest .
sudo docker push peacewang017/ai-chat-server:latest
```

# 2.2 部署在集群中
...