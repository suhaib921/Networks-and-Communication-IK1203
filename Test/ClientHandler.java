package Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // keep tract all the clients and its static becouse we want tp go through the class not each object of the class 
   
    private Socket socket; // To establish a connection betweeen client and the server
    private BufferedReader bufferedReader; // To read the data from the client
    private BufferedWriter bufferedWriter; // To write the data to the client
    private String clientUserName; // To keep track of the client name
    

    public ClientHandler(Socket socket) {
    try{
        this.socket = socket;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.clientUserName = bufferedReader.readLine();
        clientHandlers.add(this);
        broadcastMessage("SERVER: " + clientUserName +  " has entered the chat");
    } catch (IOException e) {
        closeEverything(socket, bufferedReader, bufferedWriter);
    }
    
    
    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);      
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if(!clientHandler.clientUserName.equals(clientUserName)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
               
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUserName + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
       removeClientHandler();
        try {
            if(bufferedReader!= null) bufferedReader.close();
            if(bufferedWriter!= null) bufferedWriter.close();
            if(socket!= null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
