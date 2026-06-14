#!/bin/bash

# Arrancar o serverBS em background
python3 /app/src2/serverBS.py &

# Esperar 1-2 segundos para garantir que o socket está pronto (opcional)
sleep 2

# Arrancar o servidor principal
python3 /app/src/Server.py
