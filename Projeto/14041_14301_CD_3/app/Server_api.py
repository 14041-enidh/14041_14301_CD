from flask import Flask, request, jsonify
import socket
import json

app = Flask(__name__)

DB_HOST = 'db'
DB_PORT = 12345

def contact_db(payload):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(5)
        s.connect((DB_HOST, DB_PORT))
        s.sendall(json.dumps(payload).encode('utf-8'))
        response = s.recv(65536).decode('utf-8')
    return json.loads(response)

@app.route('/load', methods=['GET'])
def load():
    file = request.args.get('file')
    result = contact_db({"action": "load", "file": file})
    return jsonify(result)

@app.route('/save', methods=['POST'])
def save():
    payload = request.get_json()
    result = contact_db({"action": "save", "file": payload['file'], "data": payload['data']})
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)