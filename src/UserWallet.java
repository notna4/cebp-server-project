import java.util.HashMap;
import java.util.Map;

public class UserWallet {
    private final String userName;
    private final Map<String, Integer> stocks;

    public UserWallet(String userName) {
        this.userName = userName;
        this.stocks = new HashMap<>();
    }

    public synchronized void addStock(String company, int quantity) {
        stocks.put(company, stocks.getOrDefault(company, 0) + quantity);
    }

    public String getUserName() {
        return userName;
    }

    public Map<String, Integer> getStocks() {
        return stocks;
    }
}
