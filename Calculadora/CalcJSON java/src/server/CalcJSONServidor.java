package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author aluno
 */
public class CalcJSONServidor extends Thread {

    private ServerSocket ss;

    /**
     * @param port the socket port number
     */
    public CalcJSONServidor(int port) {
        try {
            this.ss = new ServerSocket(port);
        } catch (IOException e) {
            this.ss = null;
            System.out.printf("Não foi possível criar o socket no porto pretendido (%d)\nDetalhes:\n", port);
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        System.out.printf("Servidor ativo no endereço %s no porto %d.\n",
                this.ss.getInetAddress().toString(), this.ss.getLocalPort());

        if (this.ss != null) {
            System.out.printf("Servidor à espera de pedidos...\n");

            for (;;) {
                try {
                    Socket s = this.ss.accept();
                    System.out.printf("Cliente (%s) ligado.\n", s.toString());

                    // Criar thread dedicada para este cliente — servidor concorrente
                    Thread t = new CalcJSONServidorDedicado(s);
                    t.start();
                } catch (IOException e) {
                    System.out.printf("Erro ao esperar por cliente.\nDetalhes:\n");
                    e.printStackTrace(System.err);
                }
            }
        }

        System.out.printf("Servidor a terminar.\n");
    }
}
