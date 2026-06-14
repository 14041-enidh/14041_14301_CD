Este README serve como relatório para o projeto da cadeira de Computação Distribuida.

Introdução:
Para iniciar o servidor, basta, abrir a aplicação do docker, fazer cd para a pasta /app do projeto na terminal e correr o código:
"docker compose up --build"
O resultado foi o conjunto das seguintes fases:

=======================================================================================================

Fase 1 - 21/04/2026

Nesta primeira fase do projeto de Computação Distribuida foram-nos pedidos diferentes objetivos, os quais com a aprendizagem dos laboratórios, fomos capazes de cumprir. A ideia principal desta fase era adaptar o trabalho prático desenvolvido na Unidade Curricular de Programação Web para um ambiente de contentores de acordo com os seguintes objetivos:

  Objetivo 1: Separar a persistência de dados do backend Web.
    Antes, o nosso "Server.py" lia e escrevia ficheiros JSON diretamente no disco com "open()". Agora essa responsabilidade passou para um servidor a parte "db". As funções loadData e saveData deixaram de usar open() para ler/escrever ficheiros diretamente no disco. Passaram a abrir uma ligação TCP ao servidor db na porta 12345. Isto separa fisicamente a lógica de negócio da lógica de armazenamento, que era o requisito central da fase.

  Objetivo 2: Comunicação por mensagens JSON.
    As mensagens trocadas via socket seguem um protocolo simples com um campo "action" ("load" ou "save"), o nome do ficheiro, e no caso do save, os dados. JSON foi a escolha natural por ser legível, fácil de serializar em Python, e por ser o formato já usado em toda a aplicação.
    
  Objetivo 3: Contentorização com Docker Compose.
    O compose.yml define uma rede bridge privada (app_network, subnet 192.168.100.0/24) e os dois serviços dentro dela. O Docker resolve automaticamente o nome db como hostname, o que é por isso que no Server.py basta escrever host = 'db' em vez de um IP fixo.

  Objetivo 4: Só o contentor Web é acessível do exterior.
    No compose.yml, apenas o flask_app tem ports: "80:80", enquanto o  contentor db não tem nenhum mapeamento de portas, o que significa que a porta 12345 nunca é acessível desde a web, fica exclusivamente acessível dentro da rede Docker interna.

=======================================================================================================

Fase 2 – 17/05/2026

Nesta segunda fase do projeto de Computação Distribuída, o objetivo principal foi introduzir uma API REST como intermediário entre o backend Web e a base de dados, tornando a arquitetura mais modular, segura e alinhada com os princípios de sistemas distribuídos.

  Objetivo 1: Introdução de uma API REST como intermediário
    Na Fase 1, o Server.py comunicava diretamente com o servidor de base de dados via socket TCP. Na Fase 2, essa comunicação direta foi eliminada. Ffoi criado um novo servidor intermédio, o Server_api.py, que funciona como ponte entre os dois, usando métodos HTTP, GET /load para carregar dados e POST /save para os guardar. O  Server.py passou a enviar pedidos HTTP a este servidor para carregar ou guardar dados, sem precisar de saber como esses dados são guardados.

  Objetivo 2: Manutenção da comunicação TCP entre a API e a Base de Dados
    O Server_db.py manteve-se sem alterações. A API REST é que assume agora o papel de cliente TCP, comunicando com o servidor de base de dados na porta 12345 através de mensagens JSON com o campo action (load ou save). Esta abordagem reutilizou o protocolo já implementado na Fase 1, minimizando o trabalho necessário.

  Objetivo 3: Arquitetura com três contentores
    O compose.yml foi atualizado para definir três serviços: flask_app, api e db. Foram criadas duas redes Docker distintas: a frontend_network (10.10.1.0/24) que liga o flask_app à api, e a backend_network (10.10.2.0/24) que liga a api ao db. Foi também criado um DockerfileAPI para construir a imagem do contentor da API, utilizando a porta 5000.
    Observação: A subnet foi alterada de 192.168.100.0/24 para 10.10.1.0/24 e 10.10.2.0/24 porque a rede criada na Fase 1 já ocupava o intervalo anterior, e o Docker não permite duas redes com endereços sobrepostos.

  Objetivo 4: Isolamento da Base de Dados
    O contentor db está exclusivamente na backend_network, sendo completamente inacessível a partir do exterior e também inacessível pelo flask_app diretamente. Apenas o contentor api consegue comunicar com ele. Isto garante um isolamento mais forte do que na Fase 1, onde o flask_app comunicava diretamente com o db.

  Objetivo 5: Único ponto de acesso exterior
    Apenas o contentor flask_app tem mapeamento de portas (80:80), sendo o único acessível pelo browser do utilizador. A api e o db não têm portas expostas ao exterior, garantindo que toda a comunicação passa obrigatoriamente pelo backend Web.

  UPDATE 11/06/2026 – Refatorização com Operações CRUD
    Após revisão do professor, foi identificada uma ineficiência na implementação anterior: sempre que era necessário aceder a um dado específico (por exemplo, um utilizador), o sistema carregava o ficheiro JSON completo e filtrava em Python o registo pretendido. 
    Para corrigir isto, foi refatorizado o protocolo de comunicação entre o Server.py e o Server_db.py, substituindo as ações genéricas load e save por operações CRUD direcionadas. Foram implementadas as seguintes ações: 
  - get_user (obtém um utilizador pelo email),
  - save_user (atualiza ou cria um utilizador),
  - get_all_products (devolve todos os produtos),
  - get_product_by_name (obtém um produto pelo nome),
  - get_confirmation, save_confirmation e delete_confirmation (gestão de tokens de confirmação de email).

No Server.py foi criada uma função base db_request() que encapsula a ligação TCP, e todas as rotas Flask passaram a invocar funções específicas como get_user(email) ou save_user(user), em vez de carregar ficheiros inteiros. Esta alteração torna o sistema mais eficiente e correto do ponto de vista de uma arquitetura distribuída.

=======================================================================================================

Fase 3 – 12/06/2026
Nesta terceira fase do projeto, o objetivo foi integrar dados de sensores IoT (Temperatura e Humidade) provenientes do laboratório do docente na interface da aplicação web do Supermercado XPTO, através de duas formas distintas de comunicação: API REST e MQTT via WebSockets.

Abordagem escolhida: Integração no Backend
  A integração foi implementada no servidor Flask (Server.py), sem alterações aos contentores Docker nem à estrutura da aplicação. Esta abordagem evita expor endereços externos no código do browser, resolve automaticamente as restrições de CORS e permite utilizar bibliotecas Python nativas para comunicação com os sistemas IoT.

Objetivo 1: Acesso via API REST
  Foi implementada a rota /weather/rest no Server.py, que usa a biblioteca requests para interrogar o endpoint REST do docente (https://cjsg.ddns.net:8443/weather/values) a cada vez que é chamada. O frontend faz fetch('/weather/rest') ao próprio servidor a cada 5 segundos, atualizando automaticamente os valores de temperatura e humidade no dashboard.

Objetivo 2: Acesso via MQTT
  Foi instalada a biblioteca paho-mqtt no contentor Flask. Quando o servidor arranca, é criada automaticamente uma ligação ao servidor do docente (cjsg.ddns.net) na porta 1883, que fica em segundo plano à espera de mensagens do tópico weather. Sempre que o sensor publica dados, estes são guardados em memória e disponibilizados através da rota /weather/mqtt. Comparando com a REST API, este modelo é mais adequado para dados em tempo real pois não requer pedidos constantes ao servidor externo.

Objetivo 3: Implementação
  Foi adicionada uma secção visual tanto na página de login (index.html) como na página principal (HomePage.html), com dois blocos distintos,um para os dados REST e outro para os dados MQTT, atualizados de 5 em 5 segundos via fetch ao backend.

Dificuldades:
  Durante os testes, o endpoint REST devolveu dados corretamente, no entanto, o cliente MQTT conectou-se com sucesso ao broker mas permaneceu em estado de espera (A aguardar...), o que reflete um erro da parte do servidor do docente, e não um erro no código implementado.

=======================================================================================================

Conclusão

O desenvolvimento deste projeto ao longo das três fases permitiu construir progressivamente uma aplicação web distribuída, partindo de uma arquitetura simples, até uma solução mas complexa, mas organizada e melhor isolada, terminando com a implementação de IoT.

Na Fase 1 foi estabelecida a separação entre o backend Web e a persistência de dados através de comunicação TCP. Na Fase 2 essa arquitetura foi aprofundada com a introdução de uma API REST como intermediário, reforçando o isolamento entre camadas e aplicando posteriormente operações CRUD direcionadas para tornar a comunicação mais eficiente. Na Fase 3 a aplicação foi estendida ao mundo IoT, integrando dados reais de sensores através de dois métodos distintos, REST e MQTT.

Ao longo das três fases é possível identificar a presença dos cinco princípios fundamentais dos sistemas distribuídos. 

- A separação em contentores independentes contribui para a Resiliência, uma vez que cada componente pode falhar ou ser reiniciado sem comprometer os restantes.
  
- A substituição das operações genéricas de leitura por operações CRUD direcionadas melhora o Desempenho e a Eficiência, evitando o carregamento desnecessário de dados.
  
- O modelo de redes isoladas, onde o contentor db só é acessível pela API, e a API só é acessível pelo Flask, implementa o princípio de Zero-Trust, em que nenhum componente confia diretamente noutro sem passar pela camada adequada.
  
- Por fim, a arquitetura modular baseada em contentores facilita a Escalabilidade, permitindo replicar ou substituir qualquer camada de forma independente.

O resultado final é uma aplicação funcional que demonstra na prática os principais conceitos de Computação Distribuída: comunicação entre processos, modelo cliente/servidor, contentorização, Web Services REST e protocolos IoT. Cada fase acrescentou uma camada de complexidade controlada, refletindo a forma como sistemas distribuídos reais evoluem, de forma incremental e modular.
