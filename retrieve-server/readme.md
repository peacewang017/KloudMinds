# 1 retrive-server 接口
retrive-server 接受文章信息和元数据，进行向量化之后存储在数据库中，对 GET 请求返回模糊化搜索的结果，接口如下：

## 1.1 上传文件
```json
${URL}/rag_upload
POST
{
    bucketname:
    filename:
    content:
}
```

## 1.2 删除文件
```json
${URL}/rag_delete
DELETE
{
    bucketname:
    filename:
}
```

## 1.3 模糊搜索
### 1.3.1 请求
```json
${URL}/rag_search
GET
{
    bucketname:
    targetcontent:
}
```

### 1.3.2 响应
```json
(若干个)
{
    filename:
    content:
}
```

# 2 容器化和部署