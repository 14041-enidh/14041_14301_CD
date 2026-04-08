package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author aluno
 */
public class CalcJSONServidorDedicado extends Thread {

    private final Socket s;

    /**
     * @param s the socket to the client
     */
    public CalcJSONServidorDedicado(Socket s) {
        this.s = s;
        System.out.println("Servidor dedicado criado.");
    }

    @Override
    public void run() {
        System.out.printf("Servidor dedicado ativo para %s.\n", this.s.getInetAddress().toString());

        BufferedReader leitor = null;
        PrintWriter    escritor = null;

        try {
            leitor   = new BufferedReader(new InputStreamReader(this.s.getInputStream(),  "UTF-8"));
            escritor = new PrintWriter(new OutputStreamWriter(this.s.getOutputStream(), "UTF-8"), true);

            for (;;) {
                // Ler uma linha JSON do cliente (bloqueia até '\n')
                String pedido = leitor.readLine();

                // readLine() devolve null quando o cliente fecha a ligação
                if (pedido == null) break;

                System.out.printf("Pedido recebido: %s\n", pedido);

                String resposta = processarPedido(pedido);

                System.out.printf("Resposta enviada: %s\n", resposta);

                // println() envia a string + '\n' (delimitador de mensagem)
                escritor.println(resposta);
            }
        } catch (IOException ioEx) {
            System.out.println("Ligação fechada.");
        } catch (Exception ex) {
            System.err.printf("Erro no servidor dedicado!\nDetalhes:\n");
            ex.printStackTrace(System.err);
        } finally {
            try {
                if (leitor   != null) leitor.close();
                if (escritor != null) escritor.close();
                this.s.close();
            } catch (Exception ex) {
                System.err.printf("Erro ao terminar servidor dedicado!\nDetalhes:\n");
                ex.printStackTrace(System.err);
            }
        }

        System.out.printf("Servidor dedicado a terminar.\n");
    }

    // Processar um pedido JSON e devolver a resposta JSON como String
    private String processarPedido(String json) {
        try {
            String tipo = extrairString(json, "type");
            String oper = extrairString(json, "oper");

            if (tipo.equals("int")) {
                int op1 = extrairInt(json, "op1");
                int op2 = extrairInt(json, "op2");
                int resultado;

                switch (oper) {
                    case "+": resultado = op1 + op2; break;
                    case "-": resultado = op1 - op2; break;
                    case "*": resultado = op1 * op2; break;
                    case "/": resultado = op1 / op2; break;
                    default:  return "{\"error\":\"Operacao desconhecida: " + oper + "\"}";
                }
                return "{\"result\":" + resultado + "}";

            } else if (tipo.equals("complex")) {
                double[] c1 = extrairComplexo(json, "op1");
                double[] c2 = extrairComplexo(json, "op2");
                double re, im;

                switch (oper) {
                    case "+":
                        re = c1[0] + c2[0];
                        im = c1[1] + c2[1];
                        break;
                    case "-":
                        re = c1[0] - c2[0];
                        im = c1[1] - c2[1];
                        break;
                    case "*":
                        re = c1[0] * c2[0] - c1[1] * c2[1];
                        im = c1[0] * c2[1] + c1[1] * c2[0];
                        break;
                    default:
                        return "{\"error\":\"Operacao complexa desconhecida: " + oper + "\"}";
                }
                if (im == 0) {
                    return "{\"result\":" + (int)re + "}";
                }
                return "{\"result\":{\"re\":" + re + ",\"im\":" + im + "}}";

            } else {
                return "{\"error\":\"Tipo desconhecido: " + tipo + "\"}";
            }

        } catch (Exception e) {
            return "{\"error\":\"Erro a processar pedido: " + e.getMessage() + "\"}";
        }
    }


    // Extrai valor string:  "oper":"+"  →  "+"
    private String extrairString(String json, String chave) {
        String padrao = "\"" + chave + "\":\"";
        int inicio = json.indexOf(padrao) + padrao.length();
        int fim    = json.indexOf("\"", inicio);
        return json.substring(inicio, fim);
    }

    // Extrai valor inteiro:  "op1":10  →  10
    private int extrairInt(String json, String chave) {
        String padrao = "\"" + chave + "\":";
        int inicio = json.indexOf(padrao) + padrao.length();
        int fim    = inicio;
        while (fim < json.length() &&
                (Character.isDigit(json.charAt(fim)) || json.charAt(fim) == '-')) fim++;
        return Integer.parseInt(json.substring(inicio, fim));
    }

    // Extrai complexo:  "op1":{"re":2,"im":3}  →  [2.0, 3.0]
    private double[] extrairComplexo(String json, String chave) {
        String padrao = "\"" + chave + "\":";
        int inicio = json.indexOf(padrao) + padrao.length();
        int abre   = json.indexOf("{", inicio);
        int fecha  = json.indexOf("}", abre);
        String bloco = json.substring(abre, fecha + 1);
        return new double[]{ extrairDouble(bloco, "re"), extrairDouble(bloco, "im") };
    }

    // Extrai valor double:  "re":2  →  2.0
    private double extrairDouble(String json, String chave) {
        String padrao = "\"" + chave + "\":";
        int inicio = json.indexOf(padrao) + padrao.length();
        int fim    = inicio;
        while (fim < json.length() &&
                (Character.isDigit(json.charAt(fim)) || json.charAt(fim) == '.' || json.charAt(fim) == '-')) fim++;
        return Double.parseDouble(json.substring(inicio, fim));
    }
}
