import java.net.*;
import java.io.*;

public class TCPClient {
    private static int BUFFERSIZE = 1024;
    private  Integer timeout;
    private  Integer limit;
    private  boolean shutdown;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.timeout = timeout;
        this.limit = limit;
        this.shutdown = shutdown;
    }

    // Main askServer Methods
    public byte[] askServer(String hostname, int port, byte[] bytesToServer) throws IOException {
       
	       	Socket socket = new Socket(hostname, port);;
	       	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream inputStreamFromServer = socket.getInputStream();
                OutputStream outToServer = socket.getOutputStream();
                byte[] readBuffer = new byte[BUFFERSIZE];
                int totalBytesRead = 0;
                int readBytes;
       	
        try {
       	        
            // Set timeout
            if (timeout != null) {
                socket.setSoTimeout(timeout);
            }

          
                // Send data to server if provided
                if (bytesToServer != null && bytesToServer.length > 0) {
                    outToServer.write(bytesToServer);
                    if (shutdown) {
                        System.out.println("SHUTDOWN SHUTDOWN!!!!!!");
                        socket.shutdownOutput(); // Shutdown output to signal server
                    }
                }
                
                if(limit != null && limit < BUFFERSIZE){ 
                 readBuffer= new byte[limit];
                }
                
                while ((readBytes = inputStreamFromServer.read(readBuffer)) != -1) {
                    
                    
                     totalBytesRead += readBytes;
                    // Stop reading if data limit is reached
                    if ( limit != null && totalBytesRead >= limit) {
                        System.out.println("Limit reached");
                        buffer.write(readBuffer, 0, readBytes);
                        break;
                    }
                
                    	buffer.write(readBuffer, 0, readBytes);
                   
                }
            
        } catch (SocketTimeoutException e) {
             System.out.println("TIMEOUT SHUTDOWN!!!!!!");
        
            
        } 
          
          socket.close();
          return buffer.toByteArray(); // Return the data collected from server
    }
}
