# KloudMinds
```
  _  ___                 _ __  __ _           _     
 | |/ / |               | |  \/  (_)         | |    
 | ' /| | ___  _   _  __| | \  / |_ _ __   __| |___ 
 |  < | |/ _ \| | | |/ _` | |\/| | | '_ \ / _` / __|
 | . \| | (_) | |_| | (_| | |  | | | | | | (_| \__ \
 |_|\_\_|\___/ \__,_|\__,_|_|  |_|_|_| |_|\__,_|___/

```                                               

<img src="images/icon.jpg" alt="KloudMinds Icon" style="width: 200px; height: auto;">

AI 驱动的云原生文件管理服务

- 文件存储 

- 快速检索 

- AI 阅读分析

# 微服务目录
```shell
> tree -L 1
.
├── ai-chat-server
├── AWS-configure
├── backend
├── EFS
├── elastic-search
├── frontend
├── images
├── information.md
├── minio
├── monitoring-logging
├── ndb-mysql
├── presentation
├── rabbit-mq
├── readme.md
├── retrieve-server
└── weaviate
```

# 部署与使用
程序由多个微服务组装而成，每个文件夹下是一个微服务/配置，每个子文件夹下均有 readme 文档，给出了详细的部署和调试引导。

请按照下面 1 -> 2 -> 3 的顺序进行配置与部署：

1. AWS-configure, EFS

2. Minio, elastic-search, monitoring-logging, ndbmysql, rabbitmq，weaviate, retrieve-server, ai-chat-server

3. backend, frontend

部署完成后，可以通过访问前端暴露出的端口，登录使用服务。