import pika
import json
import threading
import logging
from minio import Minio
from elasticsearch import Elasticsearch
from flask import Flask, request, jsonify
import fitz  # pymupdf
import docx
import os
import traceback

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Environment variables for configuration
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', '44.201.121.216')
RABBITMQ_PORT = int(os.getenv('RABBITMQ_PORT', 30007))
RABBITMQ_QUEUE = os.getenv('RABBITMQ_QUEUE', 'file-upload-exchange')

MINIO_HOST = os.getenv('MINIO_HOST', '44.201.121.216:32000')
MINIO_ACCESS_KEY = os.getenv('MINIO_ACCESS_KEY', 'minioadmin')
MINIO_SECRET_KEY = os.getenv('MINIO_SECRET_KEY', 'minioadmin')

ELASTICSEARCH_HOST = os.getenv('ELASTICSEARCH_HOST', 'http://localhost:9200')
ELASTICSEARCH_API_KEY = os.getenv('ELASTICSEARCH_API_KEY', 'N3lDX3JKQUJWbDhKWVVTcHEyemU6TWRKdFcwanFURTJIa0xnb3NGTVMtdw==')
ELASTIC_USERNAME = os.getenv('ELASTIC_USERNAME', 'elastic')
ELASTIC_PASSWORD = os.getenv('ELASTIC_PASSWORD', 'ECb576Hnv304uL2JVSae9G83')

# Initialize MinIO client
minio_client = Minio(MINIO_HOST,
                     access_key=MINIO_ACCESS_KEY,
                     secret_key=MINIO_SECRET_KEY,
                     secure=False) 

# Initialize Elasticsearch client
es = Elasticsearch(ELASTICSEARCH_HOST, 
                   api_key=ELASTICSEARCH_API_KEY)

# Initialize Flask app
app = Flask(__name__)

def parse_file(bucket_name, file_name):
    """Parses the file and returns its content as a string."""
    response = minio_client.get_object(bucket_name, file_name)
    content = response.read()
    
    if file_name.endswith('.txt'):
        return content.decode('utf-8')
    elif file_name.endswith('.docx'):
        doc = docx.Document(content)
        return '\n'.join([p.text for p in doc.paragraphs])
    elif file_name.endswith('.pdf'):
        pdf_document = fitz.open(stream=content, filetype="pdf")
        text = ""
        for page_num in range(pdf_document.page_count):
            page = pdf_document.load_page(page_num)
            text += page.get_text()
        return text
    else:
        raise ValueError(f"Unsupported file type: {file_name}")

def save_to_elasticsearch(bucket_name, file_name, content):
    """Saves the parsed content to Elasticsearch."""
    doc = {
        'bucketName': bucket_name,
        'fileName': file_name,
        'content': content
    }
    es.index(index="files", body=doc)
    logger.info("Saved to Elasticsearch")

def on_message(ch, method, properties, body):
    """Callback function to handle messages from RabbitMQ."""
    message = json.loads(body)  # Use a safe method to deserialize
    bucket_name = message['bucketName']
    file_name = message['fileName']
    
    try:
        content = parse_file(bucket_name, file_name)
        save_to_elasticsearch(bucket_name, file_name, content)
        logger.info(f"File {file_name} from bucket {bucket_name} indexed successfully.")
    except Exception as e:
        logger.error(f"Failed to process file {file_name} from bucket {bucket_name}: {str(e)}")
        logger.error(traceback.format_exc())

def start_rabbitmq_listener():
    """Starts the RabbitMQ listener."""
    try:
        connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT))
        channel = connection.channel()
        channel.exchange_declare(exchange=RABBITMQ_QUEUE, exchange_type='direct')
        channel.queue_declare(queue=RABBITMQ_QUEUE)
        channel.queue_bind(exchange=RABBITMQ_QUEUE, queue=RABBITMQ_QUEUE)
        channel.basic_consume(queue=RABBITMQ_QUEUE, on_message_callback=on_message, auto_ack=True)
        logger.info('Listening for messages...')
        channel.start_consuming()
    except pika.exceptions.AMQPConnectionError as e:
        logger.error(f'Failed to connect to RabbitMQ: {str(e)}')
        logger.error(traceback.format_exc())
    except Exception as e:
        logger.error(f'An error occurred: {str(e)}')
        logger.error(traceback.format_exc())

@app.route('/search', methods=['GET'])
def search():
    bucket_name = request.args.get('bucketname')
    target_content = request.args.get('targetcontent')
    
    if not bucket_name or not target_content:
        return jsonify({'error': 'bucketname and targetcontent are required'}), 400

    query = {
        "bool": {
            "must": [
                {"match": {"bucketName": bucket_name}},
                {"match_phrase": {"content": target_content}}
            ]
        }
    }
    
    response = es.search(index="files", body={"query": query})
    results = [hit['_source']['fileName'] for hit in response['hits']['hits']]
    
    return jsonify(results)

if __name__ == '__main__':
    # Start RabbitMQ listener in a separate thread
    threading.Thread(target=start_rabbitmq_listener).start()
    es.info()
    
    # Start Flask app
    app.run(host='0.0.0.0', port=5000)
