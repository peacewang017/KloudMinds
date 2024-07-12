from flask import Flask, request, jsonify
from elasticsearch import Elasticsearch
import logging

# 创建 Flask 应用
app = Flask(__name__)

# 连接到 Elasticsearch
es = Elasticsearch(["http://localhost:9200"])

# 设置索引名称
INDEX_NAME = "user-files"

# 设置日志
logging.basicConfig(level=logging.INFO)

@app.route('/store', methods=['POST'])
def store():
    data = request.json
    username = data.get('username')
    filename = data.get('filename')
    content = data.get('content')

    if not all([username, filename, content]):
        return jsonify({"error": "Missing fields"}), 400

    document = {
        "username": username,
        "filename": filename,
        "content": content
    }

    # 存入 Elasticsearch
    response = es.index(index=INDEX_NAME, document=document)
    logging.info(f"Document indexed: {response['_id']}")
    return jsonify({"result": "Document stored", "id": response["_id"]})

@app.route('/search', methods=['GET'])
def search():
    username = request.args.get('username')
    target = request.args.get('target')

    if not all([username, target]):
        return jsonify({"error": "Missing parameters"}), 400

    query = {
        "query": {
            "bool": {
                "must": [
                    {"match": {"username": username}},
                    {"match": {"content": target}}
                ]
            }
        }
    }

    # 搜索 Elasticsearch
    response = es.search(index=INDEX_NAME, body=query)
    results = [{"filename": hit["_source"]["filename"], "content": hit["_source"]["content"]} for hit in response["hits"]["hits"]]
    
    logging.info(f"Search results: {results}")
    return jsonify(results)

if __name__ == '__main__':
    app.run(port=5000)
