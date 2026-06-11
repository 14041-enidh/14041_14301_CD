import socket
import json
import logging
import os

HOST = '0.0.0.0'
PORT = 12345
DATA_DIR = './private'

logging.basicConfig(level=logging.DEBUG)

def handle_request(data):
    try:
        payload = json.loads(data)
        action = payload.get('action')

        # --- USERS ---
        if action == 'get_user':
            email = payload.get('email')
            users = _load_file('users.json')
            for user in users['usersInfo'][0]['user']:
                if user['email'] == email:
                    return json.dumps(user)
            return json.dumps(None)

        elif action == 'save_user':
            user_data = payload.get('user')
            users = _load_file('users.json')
            found = False
            for i, user in enumerate(users['usersInfo'][0]['user']):
                if user['email'] == user_data['email']:
                    users['usersInfo'][0]['user'][i] = user_data
                    found = True
                    break
            if not found:
                users['usersInfo'][0]['user'].append(user_data)
            _save_file('users.json', users)
            return json.dumps({"status": "ok"})

        # --- PRODUCTS ---
        elif action == 'get_all_products':
            return json.dumps(_load_file('produtos.json'))

        elif action == 'get_product_by_name':
            name = payload.get('name')
            products = _load_file('produtos.json')
            for p in products:
                if p['name'] == name:
                    return json.dumps(p)
            return json.dumps(None)

        # --- CONFIRMATIONS ---
        elif action == 'get_confirmation':
            conf_id = payload.get('id')
            data = _load_file('email_confirmations.json')
            email = data['confirmations'].get(conf_id)
            return json.dumps(email)  # None se não existir

        elif action == 'save_confirmation':
            conf_id = payload.get('id')
            email = payload.get('email')
            data = _load_file('email_confirmations.json')
            data['confirmations'][conf_id] = email
            _save_file('email_confirmations.json', data)
            return json.dumps({"status": "ok"})

        elif action == 'delete_confirmation':
            conf_id = payload.get('id')
            data = _load_file('email_confirmations.json')
            data['confirmations'].pop(conf_id, None)
            _save_file('email_confirmations.json', data)
            return json.dumps({"status": "ok"})

        else:
            return json.dumps({"status": "error", "message": "Ação desconhecida"})

    except Exception as e:
        logging.error(f"Erro: {e}")
        return json.dumps({"status": "error", "message": str(e)})


# Helpers internos
def _load_file(filename):
    filepath = os.path.join(DATA_DIR, os.path.basename(filename))
    with open(filepath, 'r', encoding='utf-8') as f:
        return json.load(f)

def _save_file(filename, data):
    filepath = os.path.join(DATA_DIR, os.path.basename(filename))
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((HOST, PORT))
        server.listen(5)
        logging.info(f"Servidor DB à escuta em {HOST}:{PORT}")

        while True:
            conn, addr = server.accept()
            with conn:
                logging.debug(f"Ligação de {addr}")
                data = conn.recv(65536)
                response = handle_request(data.decode('utf-8'))
                conn.sendall(response.encode('utf-8'))

if __name__ == '__main__':
    main()