import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ToDoServer {
    static Map<String, List<String>> userTasks = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/add", new AddTaskHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://localhost:8000/");
    }

    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            // Read the full request body, not just one line
            InputStreamReader isr = new InputStreamReader(ex.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line);
            }
            String form = buf.toString();

            Map<String, String> params = parseQuery(form);

            String user = params.get("username");
            String pass = params.get("password");

            String response;
            if ("admin".equals(user) && "1234".equals(pass)) {
                userTasks.putIfAbsent(user, new ArrayList<>());
                response = "success";
            } else {
                response = "fail";
            }

            sendResponse(ex, response);
        }
    }

    static class AddTaskHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            URI uri = ex.getRequestURI();
            String query = uri.getQuery();
            Map<String, String> params = parseQuery(query);

            String task = params.get("task");
            if (userTasks.containsKey("admin")) {
                userTasks.get("admin").add(task); // basic demo, always "admin" user
            }

            sendResponse(ex, "added");
        }
    }

    static void sendResponse(HttpExchange ex, String response) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        ex.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = ex.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length > 1) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(key, value);
            } else if (kv.length == 1) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                map.put(key, "");
            }
        }
        return map;
    }
}
