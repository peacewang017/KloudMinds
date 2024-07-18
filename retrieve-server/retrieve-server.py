import logging
from flask import Flask, request, jsonify
import os
import weaviate
from weaviate.exceptions import UnexpectedStatusCodeException
from sentence_transformers import SentenceTransformer
import requests
import spacy

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

WEAVIATE_URL = os.getenv('WEAVIATE_URL', 'http://a175176d507474fc596ea7a3791b9786-1160213758.us-east-1.elb.amazonaws.com:80')

# Initialize Weaviate client
weaviate_client = weaviate.Client(url=WEAVIATE_URL)

# Initialize model
model = SentenceTransformer('all-MiniLM-L6-v2')

# Initialize spaCy models
spacy.cli.download("en_core_web_sm")
spacy.cli.download("zh_core_web_sm")
nlp_en = spacy.load('en_core_web_sm')
nlp_zh = spacy.load('zh_core_web_sm')

# Initialize Flask app
app = Flask(__name__)

# 删除已存在的Article类
def delete_existing_class(client):
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
def create_schema(client):
    schema = {
        "classes": [
            {
                "class": "Article",
                "properties": [
                    {
                        "name": "bucketname",
                        "dataType": ["string"]
                    },
                    {
                        "name": "filename",
                        "dataType": ["string"]
                    },
                    {
                        "name": "content",
                        "dataType": ["text"]
                    },
                    {
                        "name": "vector",
                        "dataType": ["number[]"]
                    },
                    {
                        "name": "chunknum",
                        "dataType": ["int"]
                    }
                ]
            }
        ]
    }
    client.schema.create(schema)

def text_to_vector(text, model):
    return model.encode(text).tolist()

def chunk_text(text, language='en'):
    if language == 'en':
        doc = nlp_en(text)
    else:
        doc = nlp_zh(text)
    return [sent.text.strip() for sent in doc.sents]

def import_data(client, model, bucketname, filename, content, language='en'):
    chunks = chunk_text(content, language)
    for chunknum, chunk in enumerate(chunks):
        article_data = {
            "bucketname": bucketname,
            "filename": filename,
            "content": chunk,
            "chunknum": chunknum
        }
        vector = text_to_vector(chunk, model)
        client.data_object.create(
            data_object=article_data,
            class_name="Article",
            vector=vector
        )

def delete_articles(client, bucketname, filename):
    try:
        logger.info(f"Attempting to delete articles with bucketname: {bucketname} and filename: {filename}")
        
        # Optionally set the consistency level
        client.batch.consistency_level = weaviate.data.replication.ConsistencyLevel.ALL  # default QUORUM
        
        # Perform delete operation
        result = client.batch.delete_objects(
            class_name="Article",
            where={
                "operator": "And",
                "operands": [
                    {
                        "path": ["bucketname"],
                        "operator": "Equal",
                        "valueText": bucketname
                    },
                    {
                        "path": ["filename"],
                        "operator": "Equal",
                        "valueText": filename
                    }
                ]
            },
            output="verbose",
            dry_run=False
        )

        logger.info(f"Delete result: {result}")
        return result

    except UnexpectedStatusCodeException as e:
        logger.error(f"Unexpected status code error while deleting articles: {e}")
    except requests.exceptions.ConnectionError as e:
        logger.error(f"Connection error while querying Weaviate: {e}")
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}")

@app.route('/rag_upload', methods=['POST'])
def rag_upload():
    data = request.get_json()
    bucketname = data.get('bucketname')
    filename = data.get('filename')
    content = data.get('content')
    language = data.get('language', 'en')
    import_data(weaviate_client, model, bucketname, filename, content, language)
    return jsonify({"message": "Articles uploaded successfully."}), 200

@app.route('/rag_delete', methods=['DELETE'])
def rag_delete():
    data = request.get_json()
    bucketname = data.get('bucketname')
    filename = data.get('filename')
    result = delete_articles(weaviate_client, bucketname, filename)
    return jsonify(result), 200

@app.route('/rag_search', methods=['POST'])
def rag_search():
    data = request.get_json()
    bucketname = data.get('bucketname')
    targetcontent = data.get('targetcontent')
    language = data.get('language', 'en')
    response = []

    query_chunks = chunk_text(targetcontent, language)

    for chunk in query_chunks:
        query_vector = text_to_vector(chunk, model)
        near_text = {
            "concepts": [chunk],
            "vector": query_vector,
            "distance": 0.5
        }

        result = weaviate_client.query.get("Article", ["bucketname", "filename", "content", "chunknum"]).with_where({
            "path": ["bucketname"],
            "operator": "Equal",
            "valueString": bucketname
        }).with_near_vector(near_text).do()

        if result.get("data", {}).get("Get", {}).get("Article"):
            for article in result["data"]["Get"]["Article"]:
                response.append({
                    "filename": article['filename'],
                    "content": article['content']
                })
                logger.info(f"Match found: bucketname={article['bucketname']}, filename={article['filename']}, content={article['content']}, chunknum={article['chunknum']}")

    if response:
        return jsonify(response), 200
    else:
        return jsonify({"message": "No similar articles found."}), 404

if __name__ == '__main__':
    # Start Flask app
    app.run(host='0.0.0.0', port=5001)