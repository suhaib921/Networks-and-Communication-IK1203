import java.net.*;
import java.io.*;

public class ConcHTTPAsk {

    public static void main(String[] args) throws IOException{
        int port=0;

        try {
            if (args.length < 1) {
                System.err.println("Usage: java HTTPAsk <port number>");
                System.exit(1);
            }
            else if(args.length>0)
            {
             port = Integer.parseInt(args[0]);
            }
            else{
                port=8888;
            }
           
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number");
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                    System.out.println("Client connected");

                    ClientHandler clientHandler = new ClientHandler(clientSocket);

                     
                    new Thread(clientHandler).start();
                
            }
        } catch (IOException e) {
            System.err.println("Server could not start: " + e.getMessage());
        }
    }


    
}
