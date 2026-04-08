package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Cliente CalcJSON — envia operações em JSON e lê respostas.
 * @author aluno
 */
public class App extends Thread {

    private static final String DefaultHostName = "localhost";
    private static final int    DefaultPort     = 12350;

    private Socket s;

    public App(String host, int port) {
        try {
            this.s = new Socket(host, port);
            System.out.printf("Ligação estabelecida (%s).\n", this.s.toString());
        } catch (Exception e) {
            this.s = null;
            System.out.printf("Não foi possível estabelecer ligação com o servidor (%s) no porto pretendido (%d)\nDetalhes:\n", host, port);
            e.printStackTrace(System.err);
        }
    }


    // Enviar pedido JSON e ler resposta
    private String enviarPedido(PrintWriter escritor, BufferedReader leitor, String json) throws IOException {
        escritor.println(json);        // envia a linha + '\n'
        return leitor.readLine();      // bloqueia até receber a resposta
    }


    // Mostrar resultado de operação com inteiros
    private void mostrarResultadoInt(int op1, String oper, int op2, String respostaJSON) {
        int resultado = extrairInt(respostaJSON, "result");
        System.out.printf("%d %s %d = %d\n", op1, oper, op2, resultado);
    }

    // Mostrar resultado de operação com complexos
    private void mostrarResultadoComplexo(double re1, double im1,
                                          double re2, double im2,
                                          String oper, String respostaJSON) {
        // A resposta é {"result":{"re":...,"im":...}}
        int abre  = respostaJSON.indexOf("{", respostaJSON.indexOf("result"));
        int fecha = respostaJSON.lastIndexOf("}");
        String bloco = respostaJSON.substring(abre, fecha + 1);

        double re = extrairDouble(bloco, "re");
        double im = extrairDouble(bloco, "im");
        String sinal = (im >= 0) ? "+" : "";

        System.out.printf("(%.1f+%.1fi) %s (%.1f+%.1fi) = %.1f%s%.1fi\n",
                re1, im1, oper, re2, im2, re, sinal, im);
    }

    @Override
    public void run() {
        if (this.s == null) return;

        PrintWriter   escritor = null;
        BufferedReader leitor  = null;

        try {
            escritor = new PrintWriter(new OutputStreamWriter(this.s.getOutputStream(), "UTF-8"), true);
            leitor   = new BufferedReader(new InputStreamReader(this.s.getInputStream(), "UTF-8"));

            String resposta;

            // Operações com inteiros
            System.out.println("=== Operações com inteiros ===");

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"int\",\"op1\":5,\"op2\":2,\"oper\":\"+\"}");
            mostrarResultadoInt(5, "+", 2, resposta);

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"int\",\"op1\":5,\"op2\":2,\"oper\":\"-\"}");
            mostrarResultadoInt(5, "-", 2, resposta);

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"int\",\"op1\":5,\"op2\":2,\"oper\":\"*\"}");
            mostrarResultadoInt(5, "*", 2, resposta);

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"int\",\"op1\":5,\"op2\":2,\"oper\":\"/\"}");
            mostrarResultadoInt(5, "/", 2, resposta);

            // Operações com complexos
            System.out.println("\n=== Operações com complexos ===");

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"complex\",\"op1\":{\"re\":2,\"im\":3},\"op2\":{\"re\":1,\"im\":4},\"oper\":\"+\"}");
            mostrarResultadoComplexo(2, 3, 1, 4, "+", resposta);

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"complex\",\"op1\":{\"re\":2,\"im\":3},\"op2\":{\"re\":1,\"im\":4},\"oper\":\"-\"}");
            mostrarResultadoComplexo(2, 3, 1, 4, "-", resposta);

            resposta = enviarPedido(escritor, leitor,
                    "{\"type\":\"complex\",\"op1\":{\"re\":2,\"im\":3},\"op2\":{\"re\":1,\"im\":4},\"oper\":\"*\"}");
            mostrarResultadoComplexo(2, 3, 1, 4, "*", resposta);

            escritor.close();
            leitor.close();
            this.s.close();

        } catch (Exception e) {
            System.out.printf("Erro ao processar mensagens.\nDetalhes:\n");
            e.printStackTrace(System.err);
        }

        System.out.printf("Cliente a terminar.\n");
    }

    private int extrairInt(String json, String chave) {
        String padrao = "\"" + chave + "\":";
        int inicio = json.indexOf(padrao) + padrao.length();
        int fim    = inicio;
        while (fim < json.length() &&
                (Character.isDigit(json.charAt(fim)) || json.charAt(fim) == '-')) fim++;
        return Integer.parseInt(json.substring(inicio, fim));
    }

    private double extrairDouble(String json, String chave) {
        String padrao = "\"" + chave + "\":";
        int inicio = json.indexOf(padrao) + padrao.length();
        int fim    = inicio;
        while (fim < json.length() &&
                (Character.isDigit(json.charAt(fim)) || json.charAt(fim) == '.' || json.charAt(fim) == '-')) fim++;
        return Double.parseDouble(json.substring(inicio, fim));
    }

    // Main

    /**
     * @param args the command line arguments
     *
     * args[0] is the host name (string)
     * args[1] is the port number (integer)
     */
    public static void main(String[] args) {
        String host = (args.length >= 1) ? args[0] : DefaultHostName;
        int    port = (args.length >= 2) ? Integer.parseInt(args[1]) : DefaultPort;

        App cli = new App(host, port);
        cli.start();

        System.out.println("Função main do cliente a terminar...");
    }
}
