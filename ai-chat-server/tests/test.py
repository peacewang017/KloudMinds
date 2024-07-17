# kubectl port-forward service/ai-chat-server 5000:5000

import requests

# 从文件中读取 content 内容
file_path = './file1.txt'
with open(file_path, 'r', encoding='utf-8') as file:
    content = file.read().strip()  # 读取文件内容并去除首尾空白字符

# AI_chat_server
AI_chat_server = 'http://localhost:5000/request'
headers = {
    'Content-Type': 'application/json'

}
data = {
    "content": content,
    "prompt": "according to these given files, what is tian an yan?"
}

response = requests.post(AI_chat_server, headers=headers, json=data)

if response.status_code == 200:
    print(f"Success: {response.json()}")
else:
    print(f"Error: {response.status_code} - {response.json()}")
