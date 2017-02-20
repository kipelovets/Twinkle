package ru.kipelovets.Twinkle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class ParserHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        System.err.println("Reading request");
        String input;
        try {
            input = read(t.getRequestBody());
        } catch (IOException e) {
            System.err.println("Error reading request: " + e.getMessage());
            write(t, 500, "Error reading request: " + e.getMessage());
            return;
        }

        System.err.println("Parsing document");
        String response;
        try {
            response = (new HarloweProcessor(input)).getNovel().toJSON().toString();
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            write(t, 500, "Parse error: " + e.getMessage());
            return;
        }
        System.err.println("Writing response");
        try {
            write(t, 200, response);
        } catch (Exception e) {
            System.err.println("Write error: " + e.getMessage());
        }
    }

    private void write(HttpExchange t, int code, String response) throws IOException {
        byte[] responseBytes = response.getBytes();
        t.sendResponseHeaders(code, responseBytes.length);
        OutputStream os = t.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private String read(final InputStream is) throws IOException {
        final char[] buffer = new char[1024];
        final StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(is, "UTF-8")) {
            while (true) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.append(buffer, 0, rsz);
            }
        } catch (IOException ex) {
            throw ex;
        }
        return out.toString();
    }
}