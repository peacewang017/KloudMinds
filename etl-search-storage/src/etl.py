import mysql.connector
from elasticsearch import Elasticsearch
from minio import Minio
import json

# MySQL 数据库配置
db_config = {
    'user': 'your_db_user',
    'password': 'your_db_password',
    'host': 'your_db_host',
    'database': 'your_db_name'
}

# Elasticsearch 配置
es = Elasticsearch(['http://elasticsearch:9200'])

# MinIO 配置
minio_client = Minio(
    '44.200.95.160:9000',
    access_key='minioadmin',
    secret_key='minioadmin',
    secure=False
)

def fetch_data_from_db():
    conn = mysql.connector.connect(**db_config)
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM your_table")
    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    return rows

def index_data_to_es(data, index_name):
    for record in data:
        es.index(index=index_name, body=record)

def fetch_data_from_minio(bucket_name, object_name):
    response = minio_client.get_object(bucket_name, object_name)
    data = json.load(response)
    response.close()
    response.release_conn()
    return data

def fetch_all_objects_from_minio(bucket_name):
    objects = minio_client.list_objects(bucket_name, recursive=True)
    for obj in objects:
        data = fetch_data_from_minio(bucket_name, obj.object_name)
        index_data_to_es(data, '1')

if __name__ == "__main__":
    # 同步 MySQL 数据到 Elasticsearch
    # data_from_db = fetch_data_from_db()
    # index_data_to_es(data_from_db, 'your_db_index')

    # 同步 MinIO 存储桶中的所有对象到 Elasticsearch
    bucket_name = 'bucket2'
    fetch_all_objects_from_minio(bucket_name)
# 是实时同步？