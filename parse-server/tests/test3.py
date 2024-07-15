import pika
from minio import Minio
import requests
import time

# RabbitMQ configuration
RABBITMQ_HOST = '100.27.200.64'
RABBITMQ_PORT = 30007

# MinIO configuration
MINIO_HOST = '100.27.200.64'
MINIO_PORT = 32000
MINIO_ACCESS_KEY = 'minioadmin'
MINIO_SECRET_KEY = 'minioadmin'

# Elasticsearch configuration
FLASK_SERVER_URL = 'http://localhost:5000'

# Initialize MinIO client
minio_client = Minio(f"{MINIO_HOST}:{MINIO_PORT}",
                     access_key=MINIO_ACCESS_KEY,
                     secret_key=MINIO_SECRET_KEY,
                     secure=False)

# Function to send a message to RabbitMQ
def send_rabbitmq_message(bucket_name, file_name):
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT))
    channel = connection.channel()
    message = {"bucketName": bucket_name, "fileName": file_name}
    channel.basic_publish(exchange='file-upload-exchange', routing_key='', body=str(message))
    connection.close()
    print(f"Sent message to RabbitMQ: {message}")

import io

# Function to upload a file to MinIO
def upload_file_to_minio(bucket_name, file_name, content):
    if not minio_client.bucket_exists(bucket_name):
        minio_client.make_bucket(bucket_name)
    
    content_stream = io.BytesIO(content)
    content_length = len(content)
    minio_client.put_object(bucket_name, file_name, content_stream, content_length)
    
    print(f"Uploaded file to MinIO: {bucket_name}/{file_name}")

# Function to search files in Flask API
def test_rag_search(bucketname, prompt):
    """测试 rag_search 端点"""
    endpoint = f"http://localhost:5000/rag_search"
    params = {
        'bucketname': bucketname,
        'prompt': prompt
    }
    
    response = requests.get(endpoint, params=params)
    
    if response.status_code == 200:
        result = response.json()
        print("Test RAG Search Result:")
        print(result)
    else:
        print(f"Failed to get response: {response.status_code}")
        print(response.text)

# Test case
def test_server():
    bucket_name = "test-bucket"
    file_name_txt = "test-file.txt"
    file_content_txt = b"Hello, this is a test file."
    prompt = "test file"

    # Step 1: Upload a file to MinIO
    upload_file_to_minio(bucket_name, file_name_txt, file_content_txt)

    # Step 2: Send a message to RabbitMQ
    send_rabbitmq_message(bucket_name, file_name_txt)

    # Step 3: Wait for the server to process the message
    time.sleep(5)

    # Step 4: Search for the file content
    search_results = test_rag_search(bucket_name, prompt)
    print(f"Search results: {search_results}")

# Run the test
test_server()