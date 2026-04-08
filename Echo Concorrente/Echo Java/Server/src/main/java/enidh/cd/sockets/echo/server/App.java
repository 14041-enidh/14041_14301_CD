package enidh.cd.sockets.echo.server;

/**
 *
 * @author cgonc
 */
public class App {
    
    private static final int DefaultPort = 12345;
    
    /**
     * @param args the command line arguments
     * 
     * args[0] is the port number (integer)
     */
    public static void main(String[] args) {
        int port = (args.length==0) ? DefaultPort : Integer.parseInt( args[0] );

        HelloServidor srv = new HelloServidor( port );
        srv.start();

        System.out.println("Função main do servidor a terminar...");
    }
}
