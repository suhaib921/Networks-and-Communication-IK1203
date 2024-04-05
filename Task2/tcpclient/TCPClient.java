package Task2.tcpclient;
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
       
       	Socket socket = null;
       	ByteArrayOutputStream buffer = new ByteArrayOutputStream();


        try {
       	   
       	    // Create socket
            socket = new Socket(hostname, port);
            
            // Set timeout
            if (timeout != null) {
                socket.setSoTimeout(timeout);
            }

            // Get output stream to send data to server
           OutputStream outToServer = socket.getOutputStream();
                // Send data to server if provided
                if (bytesToServer != null && bytesToServer.length > 0) {
                    outToServer.write(bytesToServer);
                    if (shutdown) {
                        socket.shutdownOutput(); // Shutdown output to signal server
                    }
                }
            

               // Read response from server
                InputStream inputStreamFromServer = socket.getInputStream();
                byte[] readBuffer = new byte[BUFFERSIZE];
                long startTime = System.currentTimeMillis();
                int totalBytesRead = 0;
                int readBytes;
                
                if(limit != null && limit < BUFFERSIZE){ 
                 readBuffer= new byte[limit];
                }
                
                while ((readBytes = inputStreamFromServer.read(readBuffer)) != -1) {
                    
                    // stop reading if timeout reached
                    if(timeout!= null && System.currentTimeMillis() - startTime >= timeout){
                    System.out.println("Timeout reached");
                    break; 
                    }
                     totalBytesRead += readBytes;
                    // Stop reading if data limit is reached
                    if ( limit != null && totalBytesRead >= limit) {
                        System.out.println("Limit reached");
                        buffer.write(readBuffer, 0, readBytes);
                        break;
                    }
                
                    	buffer.write(readBuffer, 0, readBytes);
                   
                }
                socket.close();
                return buffer.toByteArray(); // Return the data collected from server
        } catch (SocketTimeoutException e) {
            // Handle socket timeout separately to throw a customized exception message
            throw new IOException("Timeout while communicating with server: " + e.getMessage());
        } finally {
            // Ensure the socket is closed in the finally block
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
       
       
    }
}
