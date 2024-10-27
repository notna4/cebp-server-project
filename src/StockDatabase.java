import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockDatabase {
    private final Map<String, StockInfo> stockData = new HashMap<>();
    private final ConcurrentHashMap<String, UserWallet> userWallets = new ConcurrentHashMap<>();

    public synchronized boolean updateStockPrice(String company, double price) {
        StockInfo stockInfo = stockData.get(company);
        if (stockInfo != null) {
            stockInfo.setPrice(price);
            return true;
        }
        return false;
    }

    public synchronized void addCompany(String company, double initialPrice, int shares) {
        stockData.put(company, new StockInfo(initialPrice, shares));
    }

    public synchronized double getStockPrice(String company) {
        StockInfo stockInfo = stockData.get(company);
        return stockInfo != null ? stockInfo.getPrice() : -1.0;
    }

    public synchronized int getStockShares(String company) {
        StockInfo stockInfo = stockData.get(company);
        return stockInfo != null ? stockInfo.getShares() : -1;
    }

    public synchronized boolean buyStock(String company) {
        StockInfo stockInfo = stockData.get(company);
        if (stockInfo != null && stockInfo.hasSharesAvailable()) {
            stockInfo.setShares(stockInfo.getShares() - 1);
            return true;
        }
        return false;
    }

    public UserWallet getUserWallet(String userName) {
        userWallets.putIfAbsent(userName, new UserWallet(userName));
        return userWallets.get(userName);
    }

    public synchronized Map<String, StockInfo> getAllStocks() {
        // unmodifiable map for thread safety
        return Collections.unmodifiableMap(stockData);
    }
}
