import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class BuyStockHandler implements HttpHandler {
    private final StockDatabase stockDatabase;
    private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public BuyStockHandler(StockDatabase stockDatabase) {
        this.stockDatabase = stockDatabase;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");

            String company1 = null, company2 = null;
            double price1 = 0.0, price2 = 0.0;
            String user1 = null, user2 = null;

            for (String param : params) {
                String[] keyValue = param.split("=");
                switch (keyValue[0]) {
                    case "company1":
                        company1 = keyValue[1];
                        break;
                    case "price1":
                        price1 = Double.parseDouble(keyValue[1]);
                        break;
                    case "user1":
                        user1 = keyValue[1];
                        break;
                    case "company2":
                        company2 = keyValue[1];
                        break;
                    case "price2":
                        price2 = Double.parseDouble(keyValue[1]);
                        break;
                    case "user2":
                        user2 = keyValue[1];
                        break;
                }
            }

            StringBuilder response = new StringBuilder();

            Thread buyThread1 = createBuyThread(company1, price1, user1, response);
            Thread buyThread2 = createBuyThread(company2, price2, user2, response);

            buyThread1.start();
            buyThread2.start();

            try {
                buyThread1.join();
                buyThread2.join();
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

    private Thread createBuyThread(String company, double price, String user, StringBuilder response) {
        return new Thread(() -> {
            lockMap.putIfAbsent(company, new ReentrantLock());
            ReentrantLock lock = lockMap.get(company);

            if (lock.tryLock()) {
                try {
                    double currentPrice = stockDatabase.getStockPrice(company);
                    int availableShares = stockDatabase.getStockShares(company);

                    if (currentPrice == price && availableShares > 0) {
                        if (stockDatabase.buyStock(company)) {
                            UserWallet userWallet = stockDatabase.getUserWallet(user);
                            // add 1 share to user's wallet
                            userWallet.addStock(company, 1);
                            response.append("User ").append(user).append(" successfully bought 1 share of ").append(company)
                                    .append(" at price ").append(price).append("\n");
                        }
                    } else if (availableShares == 0) {
                        response.append("No shares available for ").append(company).append(".\n");
                    } else {
                        response.append("User ").append(user).append(" failed to buy ").append(company)
                                .append(" - offered price ").append(price).append(" does not match current price ")
                                .append(currentPrice).append("\n");
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                response.append("User ").append(user).append(" could not buy ").append(company)
                        .append(" - another purchase is in progress.\n");
            }
        });
    }
}
