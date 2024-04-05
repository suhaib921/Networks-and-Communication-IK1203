import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPAsk {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        int port = 0;

        try {
            if (args.length < 1) {
                System.err.println("Usage: java HTTPAsk <port number>");
                System.exit(1);
            }
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number");
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    InputStream in = clientSocket.getInputStream();
                    OutputStream out = clientSocket.getOutputStream();

                    System.out.println("Client connected");
                    handleClientRequest(in, out);
                    
                    out.flush();
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server could not start: " + e.getMessage());
        }
    }

    private static void handleClientRequest(InputStream inputStream, OutputStream responseStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead= inputStream.read(buffer);
        String httpreq= new  String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
        String[] lines= httpreq.split("\\r\\n");
        String[] line= lines[0].split(" ");



       if(line.length>1 && line[0].equals("GET") && line[2].equals("HTTP/1.1")){

            String initialRequestLine=line[1];
            if(initialRequestLine.startsWith("/ask?"))
            {
                String query = initialRequestLine.substring(5); // Ta bort "/ask?"
                Map<String, String> queryParameters = extractParameters(query); 
                String hostname = queryParameters.get("hostname");
                int portNumber = Integer.parseInt(queryParameters.getOrDefault("port", "0"));
                String queryString = queryParameters.getOrDefault("string", "");
                Integer timeout = queryParameters.containsKey("timeout") ? Integer.parseInt(queryParameters.get("timeout")) : null;
                Integer limit = queryParameters.containsKey("limit") ? Integer.parseInt(queryParameters.get("limit")) : null;
                boolean shutdown = Boolean.parseBoolean(queryParameters.getOrDefault("shutdown", "false"));


                if (hostname == null || portNumber <= 0) {
                    String response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nMissing required query parameters.";
                    responseStream.write(response.getBytes(StandardCharsets.UTF_8));
                    return;
                }

                try {
                    TCPClient tcp = new TCPClient(shutdown, timeout, limit);
                    byte[] responseBytes = tcp.askServer(hostname, portNumber, queryString.getBytes());
                    String responseFromServer = new String(responseBytes, StandardCharsets.UTF_8);
                    String finalResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + responseFromServer;
                    responseStream.write(finalResponse.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    String errorResponse = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n" + e.getMessage();
                    responseStream.write(errorResponse.getBytes(StandardCharsets.UTF_8));
                }

            }

            else{
                String response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFaulty Path.";
                responseStream.write(response.getBytes(StandardCharsets.UTF_8));
            }
    }
    else{
        String response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nFaulty HTTP Method";
        responseStream.write(response.getBytes(StandardCharsets.UTF_8));
    }
        
       
    }

    private static Map<String, String> extractParameters(String query) {
        Map<String, String> queryParameters = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                queryParameters.put(key, value);
            }
        }
        return queryParameters;
    }
}
