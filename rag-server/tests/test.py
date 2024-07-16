import requests
import time

FLASK_SERVER_URL = 'http://localhost:5000'

def upload_to_weaviate(bucketname, filename, content):
    url = f'{FLASK_SERVER_URL}/rag_upload'
    data = {'bucketname': bucketname, 'filename': filename, 'content': content}
    response = requests.post(url=url, json=data)
    if response.status_code == 200:
        print("Upload successful.")
    else:
        print("Upload failed.")

def delete_from_weaviate(bucketname, filename):
    url = f'{FLASK_SERVER_URL}/rag_delete'
    data = {'bucketname': bucketname, 'filename': filename}
    response = requests.delete(url=url, json=data)
    if response.status_code == 200:
        print("Delete successful.")
    else:
        print("Delete failed.")

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

# Example usage:
upload_to_weaviate('bucket1', 'file1.txt', '电脑，是利用模拟或者数字电子技术，根据一系列指令指示并且自动执行任意算术或逻辑操作串行的设备。通用计算机因有能遵循被称为“程序”的一般操作集的能力而使得它们能够执行极其广泛的任务。计算机被用作各种工业和娱乐设备的控制系统。这包括简单的特定用途设备（如微波炉和遥控器）、工业设备（如工业机器人和集成电路），及通用设备（如个人电脑和智能手机之类的移动设备）等。')

upload_to_weaviate('bucket1', 'file2.txt', 'Lychee[3] (Lychee is a monotypic taxon and the sole member in the genus Litchi in the soapberry family, Sapindaceae.')

upload_to_weaviate('bucket1', 'file3.txt', 'Yao Ming is a Chinese basketball executive and former professional player. He played for the Shanghai Sharks of the Chinese Basketball Association (CBA) and the Houston Rockets of the National Basketball Association (NBA). Lychee is Jason\'s favourite fruit')

# Uncomment to test deletion
# delete_from_weaviate('bucket1', 'file1.txt')
# delete_from_weaviate('bucket1', 'file2.txt')
# delete_from_weaviate('bucket1', 'file3.txt')

time.sleep(10)

search_in_weaviate('bucket1', 'what is lychee and who likes it best')