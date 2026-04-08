package server;

/**
 * Ponto de entrada do servidor CalcJSON.
 * Segue o mesmo padrão do HelloServidor do professor.
 *
 * @author aluno
 */
public class App {

    private static final int DefaultPort = 12350;

    /**
     * @param args the command line arguments
     *
     * args[0] is the port number (integer)
     */
    public static void main(String[] args) {
        int port = (args.length == 0) ? DefaultPort : Integer.parseInt(args[0]);

        CalcJSONServidor srv = new CalcJSONServidor(port);
        srv.start();

        System.out.println("Função main do servidor a terminar...");
    }
}

