import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        StockDatabase stockDatabase = new StockDatabase();
        stockDatabase.addCompany("CompanyA", 100.0, 3);
        stockDatabase.addCompany("CompanyB", 200.0, 2);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/updateStock", new HandleStockMarket(stockDatabase));
        server.createContext("/viewStockMarket", new ViewStockMarketHandler(stockDatabase));
        server.createContext("/buyStock", new BuyStockHandler(stockDatabase));
        server.createContext("/viewWallet", new ViewWalletHandler(stockDatabase));

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");
    }
}