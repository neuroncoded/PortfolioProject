import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class CalculatorServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/calculate", new CalculateHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:8000/calculate");
    }

    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            Map<String, String> params = queryToMap(requestURI.getQuery());

            String response;
            try {
                double num1 = Double.parseDouble(params.getOrDefault("num1", "0"));
                double num2 = Double.parseDouble(params.getOrDefault("num2", "0"));
                String op = params.getOrDefault("op", "+");

                double result;
                switch(op) {
                    case "+": result = num1 + num2; break;
                    case "-": result = num1 - num2; break;
                    case "*": result = num1 * num2; break;
                    case "/":
                        if (num2 == 0) {
                            response = "Error: Division by zero!";
                            sendResponse(exchange, response);
                            return;
                        }
                        result = num1 / num2; break;
                    default:
                        response = "Invalid operator!";
                        sendResponse(exchange, response);
                        return;
                }
                response = "Result: " + result;
            } catch (NumberFormatException e) {
                response = "Invalid number format!";
            }

            sendResponse(exchange, response);
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            // <-- Add CORS header here
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            if(query == null) return result;
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                } else {
                    result.put(pair[0], "");
                }
            }
            return result;
        }
    }
}
