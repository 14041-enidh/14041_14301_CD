package enidh.cd.sockets.echo.server;

/**
 *
 * @author cgonc
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author cgonc
 */
public class HelloServidorDedicado extends Thread {

    private final Socket s;

    /**
     * @param s the socket to the client
     */
    public HelloServidorDedicado(Socket s) {
        this.s = s;
        
        System.out.println("Servidor dedicado criado" );
    }

    @Override
    public void run() {
        System.out.printf("Servidor dedicado ativo no endereço %s no porto %d.\n", this.s.getInetAddress().toString(), this.s.getLocalPort() );

        InputStream in = null;
        OutputStream out = null;
        
        try {
            // ordem inversa do cliente
            in = this.s.getInputStream();
            out = this.s.getOutputStream();

            boolean clientActive = true;
            for( ; clientActive ; ) {
                char c;
                StringBuilder sb = new StringBuilder();
                
                do {
                    int aux = in.read();
                    if ( aux==-1 ) {
                        System.out.println( "Ligação fechada." );
                        clientActive=false;
                        break;
                    }
                    
                    c = (char)aux;
                    
                    sb.append( c );
                } while ( c!='\n' );
                
                if ( clientActive==true ) {
                    String resposta = sb.toString().toUpperCase();
                    out.write(resposta.getBytes());
                    out.flush();
                }
            }
        } catch (IOException ex) {
            System.err.printf( "Erro no servidor dedicado!\nDetalhes:\n" );
            ex.printStackTrace( System.err );
        }
        finally {
            try {
                if ( in!=null ) {
                    in.close();
                }
                if ( out!=null ) {
                    out.close();
                }
                this.s.close();
            }
            catch (IOException ex) {
                System.err.printf( "Erro ao terminar servidor dedicado!\nDetalhes:\n" );
                ex.printStackTrace( System.err );
            }
        }

        System.out.printf("Servidor dedicado a terminar.\n");
    }
}
