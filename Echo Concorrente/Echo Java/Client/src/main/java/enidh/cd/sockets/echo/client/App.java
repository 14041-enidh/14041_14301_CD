package enidh.cd.sockets.echo.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author cgonc
 */
public class App {
    
    private static final String DefaultHostName = "localhost";
    
    private static final int DefaultPort = 12345;
      
    /**
     * @param args the command line arguments
     * 
     * args[0] is the port number (integer)
     */
    public static void main(String[] args) throws Exception {
        String host = (args.length>=1) ? args[0] : DefaultHostName; 
        
        int port = (args.length>=2) ? Integer.parseInt( args[1] ) : DefaultPort;
        
        Socket s = new Socket(host, port);
        
        System.out.printf( "Ligação (%s) estabelecida com o servidor.\n", s.toString( ));
        
        OutputStream sOut = s.getOutputStream();
        InputStream sIn = s.getInputStream();
        
        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
        
        for(;;) {
            System.out.println( "Type text:" );
            
            String line = in.readLine();
            
            if ( line.toUpperCase().equals( "END" ) ) {
                break;
            }
            
            sOut.write( (line+ "\n").getBytes() );
            sOut.flush();
            
            char c;
            while ( ( c = (char)sIn.read() ) != '\n' ) {
                System.out.print( c );
            }
            
            System.out.println();
        }
        
        sOut.close();
        sIn.close();
        
        s.close();
    }
}
