#
# Importar as bibliotecas necessárias
from flask import Flask, redirect, send_file, request, render_template, session
from flask_session import Session
from flask_mail import Mail, Message

import uuid
import logging
import json
import re
import socket

emailRegEx = "^[a-z0-9_\.\-]+@[a-z0-9\-]+\.[a-z]{2,4}$"
vatRegEx = "^[\d]{9}$"
passwordRegEx = "^[\w]{3,7}$"

app = Flask(__name__)
app.url_map.strict_slashes = False
app.config['TEMPLATES_AUTO_RELOAD'] = True
app.config["SESSION_PERMANENT"] = False
app.config["SESSION_TYPE"] = "filesystem"

app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 465
app.config['MAIL_USERNAME'] = 'aulaweb45@gmail.com'
app.config['MAIL_PASSWORD'] = 'hxprphxtmqezmjup'
app.config['MAIL_USE_TLS'] = False
app.config['MAIL_USE_SSL'] = True

Session(app)
mail = Mail(app)

logging.basicConfig(level=logging.DEBUG)


# ── Funções de acesso à DB ────────────────────────────────────────────────────

def db_request(payload):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(5)
        s.connect(('db', 12345))
        s.sendall(json.dumps(payload).encode('utf-8'))
        return json.loads(s.recv(65536).decode('utf-8'))

def get_user(email):
    return db_request({"action": "get_user", "email": email})

def save_user(user):
    return db_request({"action": "save_user", "user": user})

def get_all_products():
    return db_request({"action": "get_all_products"})

def get_product_by_name(name):
    return db_request({"action": "get_product_by_name", "name": name})

def get_confirmation(conf_id):
    return db_request({"action": "get_confirmation", "id": conf_id})

def save_confirmation(conf_id, email):
    return db_request({"action": "save_confirmation", "id": conf_id, "email": email})

def delete_confirmation(conf_id):
    return db_request({"action": "delete_confirmation", "id": conf_id})


# ── Rotas ─────────────────────────────────────────────────────────────────────

@app.route('/')
@app.route('/static')
def getRoot():
    logging.debug("Route / called...")
    return redirect("/static/index.html", code=302)

@app.route('/favicon.ico')
def getFavicon():
    logging.debug("Route /favicon.ico called...")
    return send_file("./static/favicon.ico", as_attachment=True, max_age=1)

@app.route('/produtos')
def getProdutos():
    return get_all_products()

@app.route('/account')
def account():
    if not session.get("MAIL"):
        return redirect('/formLogin')

    user = get_user(session['MAIL'])
    if user:
        return render_template('account.html', user=user)
    return "Usuário não encontrado", 404

@app.route('/go-back', methods=['POST'])
def go_back():
    return render_template('HomePage.html')

@app.route('/account/update', methods=['GET', 'POST'])
def update_account():
    if not session.get("MAIL"):
        return redirect('/formLogin')

    if request.method == 'POST':
        email = session['MAIL']
        address = request.form.get('address')
        age = request.form.get('age')
        profile_picture = request.files.get('profile_picture')

        user = get_user(email)
        if user:
            if address:
                user['address'] = address
            if age:
                user['age'] = int(age)
            if profile_picture:
                file_path = f'./static/images/profiles/{email}.jpg'
                profile_picture.save(file_path)
                user['profile_picture'] = file_path
            save_user(user)
            return redirect('/account')

        return "Usuário não encontrado", 404

    return render_template('update_account.html')

@app.route('/admin')
def adminPage():
    return render_template('admin.html')

@app.route('/cart')
def cartPage():
    return render_template('cart.html')

@app.route('/formLogin')
def buildFormLogin():
    logging.debug("Route /FormLogin called...")
    return redirect("/static/index.html", emailRegEx=emailRegEx, passwordRegEx=passwordRegEx)

@app.route('/remove_from_cart', methods=['POST'])
def remove_from_cart():
    try:
        data = request.get_json()
        index = int(data['index'])

        current_user = get_user(session.get('MAIL'))
        if not current_user:
            return {"error": "Usuário não encontrado."}, 404

        if not current_user.get('produtos') or index < 0 or index >= len(current_user['produtos']):
            return {"error": "Índice inválido!"}, 400

        removed_product = current_user['produtos'].pop(index)
        save_user(current_user)

        return {
            "message": f"Produto '{removed_product}' removido com sucesso!",
            "produtos": current_user['produtos']
        }
    except KeyError:
        return {"error": "Índice não fornecido no corpo da requisição."}, 400
    except ValueError:
        return {"error": "Índice deve ser um número válido."}, 400
    except Exception as e:
        return {"error": str(e)}, 500

@app.route('/get_session')
def get_session():
    return {
        "email": session.get("MAIL"),
        "admin": session.get("IS_ADMIN", False)
    }

@app.route('/doLogin', methods=['POST'])
def doLogin():
    logging.debug("Route /doLogin called...")

    email = request.form['email']
    password = request.form['password']

    user = get_user(email)
    if user and user['password'] == password and user['mailConfirmado'] == True:
        session['MAIL'] = email
        session['IS_ADMIN'] = user.get('admin', False)
        return buildFormProfile()

    return render_template('dadosInvalidosT.html', errorMessage="E-Mail não confirmado/Nif invalido/Password errada", redirectURL=request.referrer)

@app.route('/doAccCreate', methods=['POST'])
def doAccCreate():
    logging.debug("Route /doAccCreate called...")

    email_form = request.form['email']
    password = request.form['password']
    confirmpassword = request.form['confirm-password']

    mailCheck = re.search(emailRegEx, email_form)
    passworCheck = re.search(passwordRegEx, password)
    confirmpassworCheck = re.search(passwordRegEx, confirmpassword)

    if not mailCheck or not passworCheck or not confirmpassworCheck:
        return render_template('dadosInvalidosT.html', errorMessage="Formato dos dados inválido", redirectURL=request.referrer)

    if confirmpassword != password:
        return render_template('dadosInvalidosT.html', errorMessage="Passwords nao sao iguais!", redirectURL=request.referrer)

    novo_user = {
        "email": email_form,
        "password": password,
        "mailConfirmado": False
    }
    save_user(novo_user)
    doSendEmail(email_form)
    return redirect("/static/index.html", code=302)

@app.route('/formProfile')
def buildFormProfile():
    logging.debug("Route /buildFormProfile called...")
    if not session.get("MAIL"):
        return buildFormLogin()
    return render_template('HomePage.html')

@app.route("/doLogout")
def doLogout():
    logging.debug("Route /doLogout called...")
    session['MAIL'] = None
    return redirect("/static/index.html")

def doSendEmail(mailEnviar):
    logging.debug("doSendEmail called...")

    _subject = 'Hello from the other side!'
    _senderName = 'Pg Web Semester 24/25'
    _senderEmail = 'aulaweb45@gmail.com'

    confirmation_id = str(uuid.uuid4())
    save_confirmation(confirmation_id, mailEnviar)

    confirmation_link = f"http://localhost/confirm_email?id={confirmation_id}"
    _msgContent = f"""
    Olá!
    Clique no link abaixo para confirmar o e-mail:
    {confirmation_link}
    """

    msg = Message(
        subject=_subject,
        sender=(_senderName, _senderEmail),
        recipients=[mailEnviar]
    )
    msg.body = _msgContent
    mail.send(msg)
    return "Message sent!"

@app.route('/confirm_email')
def confirm_email():
    confirmation_id = request.args.get('id')

    if not confirmation_id:
        return render_template('dadosInvalidosT.html', errorMessage="Erro: Parâmetro de confirmação não encontrado", redirectURL=request.referrer)

    email = get_confirmation(confirmation_id)
    if not email:
        return render_template('dadosInvalidosT.html', errorMessage="Erro: ID de confirmação inválido", redirectURL=request.referrer)

    user = get_user(email)
    if user:
        user['mailConfirmado'] = True
        save_user(user)
        delete_confirmation(confirmation_id)
        logging.debug(f"E-mail confirmado para {email}")
        return redirect("/static/mailConfirmado.html")

    return render_template('dadosInvalidosT.html', errorMessage="Erro: Usuário não encontrado.", redirectURL=request.referrer)

@app.route('/product/<int:product_id>')
def product_detail(product_id):
    products = get_all_products()
    if 0 <= product_id < len(products):
        product = products[product_id]
        product['description'] = product.get('description', 'Informações detalhadas do produto ainda não estão disponíveis.')
        return render_template('product.html', product=product)
    return "Produto não encontrado", 404

@app.route('/add_to_cart', methods=['POST'])
def add_to_cart():
    if not session.get("MAIL"):
        return {"error": "Usuário não autenticado"}, 401

    email = session['MAIL']
    product_name = request.json.get('product_name')

    if not product_name:
        return {"error": "Nome do produto não fornecido"}, 400

    user = get_user(email)
    if user:
        if "produtos" not in user:
            user["produtos"] = []
        user["produtos"].append(product_name)
        save_user(user)
        return {"message": f'Produto "{product_name}" adicionado ao carrinho'}, 200

    return {"error": "Usuário não encontrado"}, 404

@app.route('/get_cart')
def get_cart():
    logging.debug("Route /get_cart called...")

    current_user = get_user(session.get('MAIL'))
    if not current_user or not current_user.get('produtos'):
        return {"produtos": []}

    products_data = get_all_products()
    cart_products = []
    for product_name in current_user['produtos']:
        product = next((p for p in products_data if p['name'] == product_name), None)
        if product:
            cart_products.append(product)

    return {"produtos": cart_products}

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)