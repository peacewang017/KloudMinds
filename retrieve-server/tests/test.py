# kubectl port-forward service/retrieve-server 5001:5001

import requests
import time

FLASK_SERVER_URL = 'http://localhost:5001'

def read_file_content(filename):
    with open(f'./{filename}', 'r', encoding='utf-8') as file:
        content = file.read()
    return content.strip()

def upload_to_weaviate(bucketname, filename, local_filename):
    content = read_file_content(local_filename)
    url = f'{FLASK_SERVER_URL}/rag_upload'
    data = {'bucketname': bucketname, 'filename': filename, 'content': content}
    response = requests.post(url=url, json=data)
    if response.status_code == 200:
        print(f"Upload successful for {filename}.")
    else:
        print(f"Upload failed for {filename}.")

def delete_from_weaviate(bucketname, filename):
    url = f'{FLASK_SERVER_URL}/rag_delete'
    data = {'bucketname': bucketname, 'filename': filename}
    response = requests.delete(url=url, json=data)
    if response.status_code == 200:
        print(f"Delete successful for {filename}.")
    else:
        print(f"Delete failed for {filename}.")

def search_in_weaviate(bucketname, keyword):
    url = f'{FLASK_SERVER_URL}/rag_search'
    data = {'bucketname': bucketname, 'targetcontent': keyword}
    response = requests.post(url, json=data)
    if response.status_code == 200:
        articles = response.json()
        if articles:
            for article in articles:
                filename = article.get('filename', '')
                content = article.get('content', '')
                print(f"File: {filename}\nContent: {content}\n")
        else:
            print("No articles found.")
    else:
        print("Search failed.")

# # Uncomment to test deletion
# delete_from_weaviate('bucket1', 'file1.txt')
# delete_from_weaviate('bucket1', 'file2.txt')
# delete_from_weaviate('bucket1', 'file3.txt')

# # Example usage:
# upload_to_weaviate('bucket1', 'file1.txt', 'file1.txt')
# upload_to_weaviate('bucket1', 'file2.txt', 'file2.txt')
# upload_to_weaviate('bucket1', 'file3.txt', 'file3.txt')

# time.sleep(10)

search_in_weaviate('bucket1', 'a meal consists of many dishes')