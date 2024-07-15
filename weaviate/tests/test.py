import weaviate
from weaviate.exceptions import UnexpectedStatusCodeException
from sentence_transformers import SentenceTransformer
import warnings

# 忽略警告
warnings.filterwarnings("ignore", category=DeprecationWarning)

# 连接到Weaviate实例（使用v3客户端）
client = weaviate.Client(
    url="http://a175176d507474fc596ea7a3791b9786-1160213758.us-east-1.elb.amazonaws.com:80",  # Weaviate实例的URL
)

# 检查并删除已存在的Article类
try:
    schema = client.schema.get()
    classes = schema['classes']
    for weaviate_class in classes:
        if weaviate_class['class'] == "Article":
            client.schema.delete_class("Article")
            print("Deleted existing class 'Article'")
except UnexpectedStatusCodeException as e:
    print(e)

# 创建Schema
schema = {
    "classes": [
        {
            "class": "Article",
            "properties": [
                {
                    "name": "title",
                    "dataType": ["string"]
                },
                {
                    "name": "content",
                    "dataType": ["text"]
                },
                {
                    "name": "vector",
                    "dataType": ["number[]"]
                }
            ]
        }
    ]
}

client.schema.create(schema)

# 使用预训练模型将文本转换为向量
model = SentenceTransformer('all-MiniLM-L6-v2')

def text_to_vector(text):
    return model.encode(text).tolist()

# 导入数据并附加向量
articles = [
    {
        "title": "Introduction to Weaviate",
        "content": "Weaviate is a vector search engine..."
    },
    {
        "title": "Weaviate: AI-Powered Search",
        "content": "Weaviate offers powerful AI-driven search capabilities."
    },
    {
        "title": "Understanding Vector Search",
        "content": "Vector search is a technique used in modern search engines."
    },
    {
        "title": "Cooking Tips for Beginners",
        "content": "Learn how to cook delicious meals with these simple tips."
    },
    {
        "title": "Travel Guide to Paris",
        "content": "Paris is a beautiful city with many attractions to see."
    },
    {
        "title": "Latest Advances in AI",
        "content": "AI technology is rapidly evolving and transforming industries."
    }
]

for article_data in articles:
    vector = text_to_vector(article_data["content"])
    client.data_object.create(
        data_object=article_data,
        class_name="Article",
        vector=vector
    )

# 查询数据并读取向量
result = client.query.get("Article", ["title", "content", "_additional {id, vector}"]).do()

if "data" in result and "Get" in result["data"] and "Article" in result["data"]["Get"]:
    for article in result["data"]["Get"]["Article"]:
        print("Title:", article["title"])
        print("Content:", article["content"])
        print("Vector:", article["_additional"].get("vector", "None"))

    # 更新向量（示例）
    article_id = result["data"]["Get"]["Article"][0]["_additional"]["id"]
    new_content = "Weaviate is a powerful vector search engine that uses AI..."
    new_vector = text_to_vector(new_content)

    client.data_object.update(
        data_object={
            "content": new_content,
            "vector": new_vector
        },
        class_name="Article",
        uuid=article_id
    )

    # 使用向量进行相似度查询
    query_vector = text_to_vector("What is Weaviate?")

    near_text = {
        "concepts": ["What is Weaviate?"],
        "vector": query_vector,
        "distance": 0.7  # 可以调整距离阈值以控制查询结果的相似度
    }

    result = client.query.get("Article", ["title", "content"]).with_near_vector(near_text).do()

    print("\nSimilar articles:")
    for article in result["data"]["Get"]["Article"]:
        print("Title:", article["title"])
        print("Content:", article["content"])
else:
    print("No articles found.")
