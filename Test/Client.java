import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;

public class Client{
     private Socket socket;
     private BufferedReader bufferedReader;
     private BufferedWriter bufferedWriter;
     private String clientUserName;

     public Client(Socket socket, String clientUserName) throws IOException {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = clientUserName;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void sendMessage() {
        try {
            bufferedWriter.write(clientUserName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend= scanner.nextLine();

                
                bufferedWriter.write(clientUserName +" : "+  messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    // listens to broadcast messages from the client handler
    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String messageFromGroupChat;
                    while (socket.isConnected()) {
                        messageFromGroupChat = bufferedReader.readLine();
                        System.out.println(messageFromGroupChat);
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
     try {
            if(bufferedReader!= null) bufferedReader.close();
            if(bufferedWriter!= null) bufferedWriter.close();
            if(socket!= null) socket.close();
        } 
        catch (IOException e) {
                e.printStackTrace();
        }
    }
         
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        String clientUserName = scanner.nextLine();
        Socket socket = new Socket("localhost", 9999);
        Client client = new Client(socket, clientUserName);
        client.listenForMessages();
        client.sendMessage();
        client.closeEverything(socket, client.bufferedReader, client.bufferedWriter);
    }





}