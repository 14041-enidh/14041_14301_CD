package enidh.cd.sockets.echo.server;

/**
 *
 * @author cgonc
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author cgonc
 */
public class HelloServidor extends Thread {

    private ServerSocket ss;

    /**
     * @param port the socket port number
     */
    public HelloServidor(int port) {
        try {
            this.ss = new ServerSocket(port);
        } catch (IOException e) {
            this.ss = null;
            System.out.printf("Não foi possível criar o socket no porto pretendio (%d)\nDetalhes:\n", port);
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        System.out.printf("Servidor ativo no endereço %s no porto %d.\n", this.ss.getInetAddress().toString(), this.ss.getLocalPort() );

        if ( this.ss != null ) {
            System.out.printf("Servidor espera pedidos...\n");

            for (;;) {
                try {
                    Socket s = this.ss.accept();
                    System.out.printf("Cliente (%s) ligado.\n", s.toString( ));
                    
                    Thread t = new HelloServidorDedicado( s );
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
