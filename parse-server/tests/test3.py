import requests

# 配置服务器地址和端口
SERVER_URL = 'http://localhost:5000'

def test_rag_search(bucketname, prompt):
    """测试 rag_search 端点"""
    endpoint = f"{SERVER_URL}/rag_search"
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

if __name__ == '__main__':
    # 测试参数
    bucketname = 'example-bucket'
    prompt = 'Please summarize the document.'
    
    test_rag_search(bucketname, prompt)