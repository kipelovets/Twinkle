package ru.kipelovets.Twinkle;

import com.sun.net.httpserver.HttpServer;
import org.attoparser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: ");
            System.out.println("\t<app> serve <port>");
            System.out.println("\t<app> parse <filename>");
            return;
        }

        switch (args[0]) {
            case "parse":
                try {
                    String document;
                    try {
                        document = read(args[1]);
                    } catch (IOException e) {
                        System.out.println("Error reading file");
                        return;
                    }

                    System.out.println((new HarloweProcessor(document)).getNovel().toJSON().toString());
                } catch (ParseException | FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case "serve":
                int port = Integer.parseInt(args[1]);
                System.err.println("Starting server on port " + port);
                HttpServer server;
                try {
                    server = HttpServer.create(new InetSocketAddress(port), 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                server.createContext("/", new ParserHandler());
                server.setExecutor(null);
                server.start();
                break;
            default:
                System.out.println("Unknown command");
                break;
        }
    }

    public static String read(String fileName) throws IOException {
        Reader reader = new FileReader(fileName);
        final StringBuilder out = new StringBuilder();
        final char[] buffer = new char[1024];
        while (true) {
            int rsz = reader.read(buffer, 0, buffer.length);
            if (rsz < 0) {
                break;
            }
            out.append(buffer, 0, rsz);
        }

        return out.toString();
    }
}
