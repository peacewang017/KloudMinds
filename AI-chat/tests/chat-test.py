import requests

# AI_char_server
AI_chat_server = 'http://localhost:5000/receive'
headers = {
    'Content-Type': 'application/json'
}
data = {
    "prompt": "Translate the following text to French.",
    "file": "Hello, how are you?"
}

response = requests.post(AI_chat_server, headers=headers, json=data)

if response.status_code == 200:
    print(f"Success: {response.json()}")
else:
    print(f"Error: {response.status_code} - {response.json()}")