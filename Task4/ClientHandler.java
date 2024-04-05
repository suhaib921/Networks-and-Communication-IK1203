import java.net.Socket;
import java.net.URLDecoder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            handleClientRequest(in, out);

            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        }
    }

    private void handleClientRequest(InputStream inputStream, OutputStream responseStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        String httpRequest = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
        String[] lines = httpRequest.split("\\r\\n");
        String[] requestLine = lines[0].split(" ");

        if (requestLine.length > 1 && requestLine[0].equals("GET") && requestLine[2].equals("HTTP/1.1")) {
            String initialRequestLine = requestLine[1];

            if (initialRequestLine.startsWith("/ask?")) {
                String query = initialRequestLine.substring(5); // Remove "/ask?"
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
            } else {
                String errorResponse = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n";
                responseStream.write(errorResponse.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            String response = "HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain\r\n\r\nFaulty HTTP Method";
            responseStream.write(response.getBytes(StandardCharsets.UTF_8));

        }
    }

    private Map<String, String> extractParameters(String query) {
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
