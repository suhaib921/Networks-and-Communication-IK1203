import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Server {
   private ServerSocket serverSocket;

   public Server(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
   }

   public void startServer() throws Exception {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected.");
                
                TCPClientHandler clientHandler = new TCPClientHandler(socket); //run the clas runnable

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage()); 
        }
    }

    public void closeServerSocket(){
        try {
            if(serverSocket != null){
            serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9999);
        Server server = new Server(serverSocket);
        server.startServer(); 
    }

}
