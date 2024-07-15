import pika
from minio import Minio
import requests
import time

# RabbitMQ configuration
RABBITMQ_HOST = '44.201.121.216'
RABBITMQ_PORT = 30007

# MinIO configuration
MINIO_HOST = '44.201.121.216'
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
    message = {"bucketName": bucket_name, "fileName": file_name, "userId": "123"}
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
def search_files(bucket_name, target_content):
    response = requests.get(f"{FLASK_SERVER_URL}/search", params={"bucketname": bucket_name, "targetcontent": target_content})
    print(f"Response status code: {response.status_code}")
    print(f"Response text: {response.text}")
    try:
        return response.json()
    except Exception as e:
        print(f"Exception occurred while decoding JSON: {e}")
        return None


# Test case
def test_server():
    bucket_name = "test-bucket"
    file_name_txt = "test-file.txt"
    file_content_txt = b"Hello, this is a test file."
    target_content = "test file"

    # Step 1: Upload a file to MinIO
    upload_file_to_minio(bucket_name, file_name_txt, file_content_txt)

    # Step 2: Send a message to RabbitMQ
    send_rabbitmq_message(bucket_name, file_name_txt)

    # Step 3: Wait for the server to process the message
    time.sleep(5)

    # Step 4: Search for the file content
    search_results = search_files(bucket_name, target_content)
    print(f"Search results: {search_results}")

# Run the test
test_server()