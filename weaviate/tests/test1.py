import weaviate

# 配置 Weaviate 客户端
client = weaviate.Client(
    url="http://a175176d507474fc596ea7a3791b9786-1160213758.us-east-1.elb.amazonaws.com:80",  # Weaviate HTTP 服务的地址
)

# 检查 Weaviate 服务是否可用
if client.is_ready():
    print("Connected to Weaviate successfully!")
else:
    print("Failed to connect to Weaviate.")