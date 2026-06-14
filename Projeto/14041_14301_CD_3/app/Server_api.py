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

@app.route('/get_user', methods=['GET'])
def get_user():
    email = request.args.get('email')
    result = contact_db({"action": "get_user", "email": email})
    return jsonify(result)

@app.route('/save_user', methods=['POST'])
def save_user():
    user = request.get_json()
    result = contact_db({"action": "save_user", "user": user})
    return jsonify(result)

@app.route('/get_all_products', methods=['GET'])
def get_all_products():
    result = contact_db({"action": "get_all_products"})
    return jsonify(result)

@app.route('/get_product_by_name', methods=['GET'])
def get_product_by_name():
    name = request.args.get('name')
    result = contact_db({"action": "get_product_by_name", "name": name})
    return jsonify(result)

@app.route('/get_confirmation', methods=['GET'])
def get_confirmation():
    conf_id = request.args.get('id')
    result = contact_db({"action": "get_confirmation", "id": conf_id})
    return jsonify(result)

@app.route('/save_confirmation', methods=['POST'])
def save_confirmation():
    payload = request.get_json()
    result = contact_db({"action": "save_confirmation", "id": payload['id'], "email": payload['email']})
    return jsonify(result)

@app.route('/delete_confirmation', methods=['DELETE'])
def delete_confirmation():
    conf_id = request.args.get('id')
    result = contact_db({"action": "delete_confirmation", "id": conf_id})
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)