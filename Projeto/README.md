Este README serve como relatório para o projeto da cadeira de Computação Distribuida.
Introdução:
____________________
Para iniciar o servidor, basta, abrir a aplicação do docker, fazer cd para a pasta /app na terminal e correr o código:
"docker compose up --build"
O resultado foi o conjunto das seguintes fases:

21/04/2026

Fase 1:
Nesta primeira fase do projeto de Computação Distribuida foram-nos pedidos diferentes objetivos, os quais com a aprendizagem dos laboratórios, fomos capazes de cumprir. A ideia principal desta fase era adaptar o trabalho prático desenvolvido na Unidade Curricular de Programação Web para um ambiente de contentores de acordo com os seguintes objetivos:

  Objetivo 1: Separar a persistência de dados do backend Web.
    Antes, o nosso "Server.py" lia e escrevia ficheiros JSON diretamente no disco com "open()". Agora essa responsabilidade passou para um servidor a parte "db". As funções loadData e saveData deixaram de usar open() para ler/escrever ficheiros diretamente no disco. Passaram a abrir uma ligação TCP ao servidor db na porta 12345. Isto separa fisicamente a lógica de negócio da lógica de armazenamento, que era o requisito central da fase.

  Objetivo 2: Comunicação por mensagens JSON.
    As mensagens trocadas via socket seguem um protocolo simples com um campo "action" ("load" ou "save"), o nome do ficheiro, e no caso do save, os dados. JSON foi a escolha natural por ser legível, fácil de serializar em Python, e por ser o formato já usado em toda a aplicação.
    
  Objetivo 3: Contentorização com Docker Compose.
    O compose.yml define uma rede bridge privada (app_network, subnet 192.168.100.0/24) e os dois serviços dentro dela. O Docker resolve automaticamente o nome db como hostname, o que é por isso que no Server.py basta escrever host = 'db' em vez de um IP fixo.

  Objetivo 4: Só o contentor Web é acessível do exterior.
    No compose.yml, apenas o flask_app tem ports: "80:80", enquanto o  contentor db não tem nenhum mapeamento de portas, o que significa que a porta 12345 nunca é acessível desde a web, fica exclusivamente acessível dentro da rede Docker interna.
