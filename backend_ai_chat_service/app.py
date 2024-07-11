import os
from flask import Flask, request, jsonify
from openai import OpenAI

app = Flask(__name__)

# Initialize the OpenAI client
client = OpenAI(
    api_key="0de8399931df7b7e632c87930f8c6ad3",
    base_url="https://open.bigmodel.cn/api/paas/v4/"
)

@app.route("/summarize", methods=["POST"])
def summarize():
    try:
        data = request.json
        if not data or "document_content" not in data:
            return jsonify({"error": "Invalid input"}), 400

        document_content = data["document_content"]
        
        completion = client.chat.completions.create(
            model="glm-4",
            messages=[
                {"role": "system", "content": "You are a Drive Management Assistant."},
                {"role": "user", "content": f"Please summarize the following document: {document_content}"}
            ],
            top_p=0.7,
            temperature=0.9
        )

        summary = completion.choices[0].message["content"]
        return jsonify({"summary": summary}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/qa", methods=["POST"])
def qa():
    try:
        data = request.json
        if not data or "document_content" not in data or "question" not in data:
            return jsonify({"error": "Invalid input"}), 400

        document_content = data["document_content"]
        question = data["question"]

        completion = client.chat.completions.create(
            model="glm-4",
            messages=[
                {"role": "system", "content": "You are a Drive Management Assistant."},
                {"role": "user", "content": f"Please answer the following question based on the document: {document_content} Question: {question}"}
            ],
            top_p=0.7,
            temperature=0.9
        )

        answer = completion.choices[0].message["content"]
        return jsonify({"answer": answer}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
