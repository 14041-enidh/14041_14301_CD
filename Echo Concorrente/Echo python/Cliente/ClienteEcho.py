import socket

HOST  = "localhost"
PORTA = 12345  # port do servidor Java

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORTA))
print("Ligado ao servidor em {}:{}\n".format(HOST, PORTA))

while True:
    # Ler texto do utilizador
    texto = input("Escreve texto (ou 'END' para sair): ")

    if texto.upper() == "END":
        break

    # Enviar texto ao servidor com '\n' no fim (o servidor Java lê até '\n')
    sock.sendall((texto + "\n").encode())

    # Receber resposta — lemos byte a byte até ao '\n'
    resposta = ""
    while True:
        c = sock.recv(1).decode()
        if c == "\n":
            break
        resposta += c

    print("Servidor respondeu: {}".format(resposta))

sock.close()
print("Ligação encerrada.")
