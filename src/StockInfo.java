public class StockInfo {
    private double price;
    private int shares;

    public StockInfo(double price, int shares) {
        this.price = price;
        this.shares = shares;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public boolean hasSharesAvailable() {
        return shares > 0;
    }
}
