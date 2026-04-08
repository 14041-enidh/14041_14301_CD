Este README serve como relatório para o laboratório da calculadora com clientes e servidores que comunicam intercaladamente entre si.

1- Introdução
  O laboratório tinha o objetivo de estudar e implementar o modelo de comunicação cliente/servidor utilizando sockets com as lingus de programação Python e Java.
  
2- Sockets
  Um socket é uma interface de programação que permite a comunicação entre dois processos através de uma rede de computadores. Um programa escreve dados num socket e outro programa lê esses dados.
   Nos exemplos desenvolvidos foram utilizados sockets TCP (SOCK_STREAM), que garantem uma ligação fiável entre cliente e servidor, com confirmação de entrega dos dados.
   
3- Modelo Cliente/Servidor
  O modelo cliente/servidor é um paradigma de comunicação em que:
    •	O servidor fica à escuta numa porta, à espera de ligações de clientes.
    •	O cliente inicia a ligação ao servidor e envia os pedidos.
    •	O servidor processa os pedidos e devolve a resposta ao cliente.

4- Tipos de servidor
  Um servidor iterativo atende um cliente de cada vez, enquanto trata um cliente, os restantes ficam em espera, no entanto, neste laboratório usamos servidores concorrentes, que criam uma thread dedicada para cada cliente, permitindo atender múltiplos clientes em simultâneo.

5- JSON
  Originalmente o código utilizava um protocolo binário para a troca de mensagens, os dados eram enviados como bytes crus, o que implicava cuidados com o formato dos inteiros e não era legivel por humanos, portanto, o protocolo foi substituído por mensagens JSON, um formato de texto legível por humanos, independente da linguagem de programação e sem problemas de compatibilidade de formato. Esta mudança permitiu também adicionar suporte a operações com números complexos de forma mais simples e flexível.

6- Conclusão
  Este laboraatório serviu-nos bastante para estudar e aprender a implementar os conceitos estudados nas aulas, mencionados neste relatório, tais como Sockets, modelos de comunicação e JSON, o que nos vai ajudar futuramente na implementação destes conceitos no nosso projeto final.
