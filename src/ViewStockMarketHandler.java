import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ViewStockMarketHandler implements HttpHandler {
    private final StockDatabase stockDatabase;

    public ViewStockMarketHandler(StockDatabase stockDatabase) {
        this.stockDatabase = stockDatabase;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("Current Stock Market Status:\n");

        for (Map.Entry<String, StockInfo> entry : stockDatabase.getAllStocks().entrySet()) {
            response.append("Company: ").append(entry.getKey())
                    .append(", Price: ").append(entry.getValue().getPrice())
                    .append(", Shares Available: ").append(entry.getValue().getShares()).append("\n");
        }

        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
