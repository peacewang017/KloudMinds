parse2ES-server 连接到一个 MInIO 对象存储，一个 RabbitMQ 消息队列，和一个 elastic-search：

# 1 rabbitmq 数据格式
rabbitmq 中的消息格式为（即 Java 后端是这样放入的格式）：

{
    bucketname:

    filename:
}

# 2 MinIO 数据格式
bucket - filename - filecontent

# 3 elastic search 数据格式
{
    buckername:

    filename:

    content:
}

# 4 weaviate 数据格式
{
    bucketname:
    filename:
    contentchunk:
    chunkid
}

# 5 search 格式

# 6 RAG 格式
{
    bucketname:
    prompt:
}

# 5 parser server 行为

## 保存
这个 server 监听 rabbitmq，在里面有消息的时候，根据消息的 bucketname，filename 获取 MinIO 中文件的内容，保存为 content（如果 filename 后缀是 txt，直接转为字符串;如果后缀是 docx，doc，pdf 则需要调库解析为字符串）。

将 bucketname，filename，content 三个字符串传入 elastic-search 中。

## elastic-search 搜索
对 elastic-search 发送一个请求，包含一个 bucketname 字符串和一个 targetcontent，返回所有满足 （bucketname 匹配）&&（content 包含 targetcontent）的 filename。

## RAG
