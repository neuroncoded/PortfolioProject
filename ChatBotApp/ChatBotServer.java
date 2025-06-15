import com.sun.net.httpserver.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatBotServer {
    static Map<String, Map<String, String>> userMemory = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/chat", new ChatHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8000/chat");
    }

    static class ChatHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String userIp = exchange.getRemoteAddress().getAddress().getHostAddress();

            // Parse query
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            String message = queryToMap(query).getOrDefault("message", "").toLowerCase();

            // Reset logic
            if (message.equals("reset")) {
                userMemory.remove(userIp);
                sendResponse(exchange, "Memory wiped. Let’s start fresh!");
                return;
            }

            String response = generateResponse(message, userIp);
            sendResponse(exchange, response);
        }

        private void sendResponse(HttpExchange ex, String response) throws IOException {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            if (query == null) return result;
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
                } else {
                    result.put(pair[0], "");
                }
            }
            return result;
        }
    }

    static String generateResponse(String input, String userIp) {
        userMemory.putIfAbsent(userIp, new HashMap<>());
        Map<String, String> memory = userMemory.get(userIp);

        String lastQuestion = memory.get("lastQuestion");

        if (lastQuestion == null) {
            memory.put("lastQuestion", "name");
            return "Hey there! What’s your name?";
        }

        switch (lastQuestion) {
            case "name":
                memory.put("name", capitalize(input));
                memory.put("lastQuestion", "hobby");
                return "Nice to meet you, " + memory.get("name") + "! What’s your favorite hobby?";
            case "hobby":
                memory.put("hobby", input);
                memory.put("lastQuestion", "color");
                return "Cool! I’ve never tried " + input + ", sounds fun. What’s your favorite color?";
            case "color":
                memory.put("color", input);
                memory.put("lastQuestion", "done");
                return "Okay okay... so your name’s " + memory.get("name") +
                        ", you love " + memory.get("hobby") +
                        ", and your favorite color is " + memory.get("color") +
                        ". I like your vibe. 😊 Want to ask *me* something now?";
            case "done":
                if (input.contains("joke")) {
                    return "Why don't Java developers wear glasses? Because they can't C#! 😎";
                }
                return "You can ask me anything now, " + memory.get("name") +
                        "! Or type 'reset' to start over.";
            default:
                memory.put("lastQuestion", "name");
                return "Let’s restart. What’s your name?";
        }
    }

    static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0,1).toUpperCase() + str.substring(1);
    }
}
