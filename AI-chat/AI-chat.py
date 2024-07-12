import os
import requests
from flask import Flask, request, jsonify
from openai import OpenAI

app = Flask(__name__)

# 硬编码 OpenAI API
client = OpenAI(api_key='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')

# 环境变量引入 OpenAI API
# api_key = os.getenv('OPENAI_API_KEY')
# client = OpenAI(api_key=api_key)

# 处理 POST 请求
@app.route('/receive', methods=['POST'])
def receive_data():
    try:
        # 处理输入
        data = request.json
        prompt = data.get('prompt')
        file_content = data.get('file')

        if not prompt or not file_content:
            return jsonify({"error": "Invalid data"}), 400

        # 调用 OpenAI API
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "user", "content": f"{file_content}\n\n{prompt}"}
            ],
            max_tokens=150,
            temperature=0.7
        )

        ai_answer = response.choices[0].text.strip()

        if response.status_code == 200:
            return jsonify({"status": "success", "ai_answer": ai_answer}), 200
        else:
            return jsonify({"error": "Failed to send data back"}), 500

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)