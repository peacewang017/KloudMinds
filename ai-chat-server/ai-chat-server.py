import os
from flask import Flask, request, jsonify
from openai import OpenAI

app = Flask(__name__)

# 硬编码 API-key
client = OpenAI(
    api_key=os.getenv('OPENAI_API_KEY', '0de8399931df7b7e632c87930f8c6ad3.oobORQw8lNN5oIoR'),
    base_url=os.getenv('LLM_URL', 'https://open.bigmodel.cn/api/paas/v4/')
)

# 环境变量引入 OpenAI API
# api_key = os.getenv('OPENAI_API_KEY')
# client = OpenAI(api_key=api_key)

@app.route('/request', methods=['POST'])
def receive_data():
    try:
        # 处理输入
        data = request.json
        file_content = data.get('content')
        prompt = data.get('prompt')

        if not prompt or not file_content:
            return jsonify({"error": "Invalid data"}), 400

        # 调用 API
        response = client.chat.completions.create(
            model="glm-4",
            messages=[
                {"role": "user", "content": f"{file_content}\n\n{prompt}"}
            ],
            top_p=0.7,
            temperature=0.7
        )

        ai_answer = response.choices[0].message.content

        return jsonify({"status": "success", "ai_answer": ai_answer}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)