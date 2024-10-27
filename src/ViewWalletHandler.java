import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ViewWalletHandler implements HttpHandler {
    private final StockDatabase stockDatabase;

    public ViewWalletHandler(StockDatabase stockDatabase) {
        this.stockDatabase = stockDatabase;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");
            String userName = null;

            for (String param : params) {
                String[] keyValue = param.split("=");
                if ("user".equals(keyValue[0])) {
                    userName = keyValue[1];
                }
            }

            String response;
            if (userName != null) {
                UserWallet wallet = stockDatabase.getUserWallet(userName);

                StringBuilder walletInfo = new StringBuilder();
                walletInfo.append("User Wallet for ").append(wallet.getUserName()).append(":\n");
                for (Map.Entry<String, Integer> entry : wallet.getStocks().entrySet()) {
                    walletInfo.append(" - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" shares\n");
                }

                response = walletInfo.toString();
            } else {
                response = "User not found or username not provided.";
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
