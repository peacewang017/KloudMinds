from flask import Flask, request, jsonify
from elasticsearch import Elasticsearch

app = Flask(__name__)

# 连接到 Elasticsearch
es = Elasticsearch(['http://elasticsearch:9200'])

@app.route('/search', methods=['GET'])
def search():
    query = request.args.get('query')
    response = es.search(
        index='documents',
        body={
            'query': {
                'match': {
                    'content': query
                }
            }
        }
    )
    return jsonify(response['hits']['hits'])

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)