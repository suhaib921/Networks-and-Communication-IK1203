import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClient {
    private static int BUFFERSIZE = 1024;
    
    public TCPClient() {
        // No-arg constructor
    }

    //Main askServer Methods
    public byte[] askServer(String hostname, int port, byte[] bytesToServer) throws IOException {
        Socket socket = null;
        try{

            // Create socket
            socket = new Socket(hostname, port);
            
            // Get input and output streams
            InputStream inputStreamFromServer = socket.getInputStream();
            OutputStream outToServer = socket.getOutputStream();


            // Send data to server if bytesToServer is not empty
            if (bytesToServer != null && bytesToServer.length > 0) {
                outToServer = socket.getOutputStream();
                outToServer.write(bytesToServer);
                outToServer.flush(); // Ensure data is sent
            }
            

            // Read response from server
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] readbuffer = new byte[BUFFERSIZE]; // Temporary buffer
            int readBytes;
            while ((readBytes = inputStreamFromServer.read(readbuffer)) != -1) {
                buffer.write(readbuffer, 0, readBytes);
            }

            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new IOException("Error communicating with server: " + e.getMessage());
        }
        finally {
            if(socket!= null) {
                socket.close();
            }
        }
    }
}
