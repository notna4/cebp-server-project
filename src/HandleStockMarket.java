import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class HandleStockMarket implements HttpHandler {
    private final StockDatabase stockDatabase;
    private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public HandleStockMarket(StockDatabase stockDatabase) {
        this.stockDatabase = stockDatabase;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");

            String company1 = null, company2 = null;
            double price1 = 0.0, price2 = 0.0;

            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("company1")) {
                    company1 = keyValue[1];
                } else if (keyValue[0].equals("price1")) {
                    price1 = Double.parseDouble(keyValue[1]);
                } else if (keyValue[0].equals("company2")) {
                    company2 = keyValue[1];
                } else if (keyValue[0].equals("price2")) {
                    price2 = Double.parseDouble(keyValue[1]);
                }
            }

            StringBuilder response = new StringBuilder();

            Thread updateThread1 = createUpdateThread(company1, price1, response);
            Thread updateThread2 = createUpdateThread(company2, price2, response);

            updateThread1.start();
            updateThread2.start();

            try {
                updateThread1.join();
                updateThread2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                response.append("Thread interrupted.");
            }

            String finalResponse = response.toString();
            exchange.sendResponseHeaders(200, finalResponse.length());
            OutputStream os = exchange.getResponseBody();
            os.write(finalResponse.getBytes());
            os.close();
        }
    }

    private Thread createUpdateThread(String company, double price, StringBuilder response) {
        return new Thread(() -> {
            // initialize lock for company
            lockMap.putIfAbsent(company, new ReentrantLock());
            ReentrantLock lock = lockMap.get(company);

            // attempting to lock for updating
            if (lock.tryLock()) {
                try {
                    if (stockDatabase.updateStockPrice(company, price)) {
                        response.append("Stock price updated for ").append(company).append(" to ").append(price).append("\n");
                    } else {
                        response.append("Stock update failed for ").append(company).append(". Company not found.\n");
                    }
                } finally {
                    // releasing lock after updating
                    lock.unlock();
                }
            } else {
                response.append("Could not update stock for ").append(company).append(" - another update is in progress.\n");
            }
        });
    }
}
