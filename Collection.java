public class Collection {
    public int ISBN;
    public String owner_name;
    public int unit_in_stock;
    public int unit_sold;
    public double revenue;
    public double expense;
    public double profit;
    public double publisher_split;
    public Book book;
    public Publisher publisher;

    public Collection() {
        this.unit_in_stock = -1;
        this.unit_sold = -1;
        this.revenue = -1;
        this.expense = -1;
        this.profit = -1;
        this.publisher_split = -1;
        this.book = null;
        this.publisher = null;
    }

    public double getPublisherSplitPerBook() {
        return publisher_split*book.price;
    }

    public String getRecord() {
        String s = "";
        s += String.format("\tUnit in stock: %d\n", this.unit_in_stock);
        s += String.format("\tUnit sold: %d\n", this.unit_sold);
        s += String.format("\tRevenue: $%f\n", this.revenue);
        s += String.format("\tExpense: $%f\n", this.expense);
        s += String.format("\tPublisher split ratio: %2.0f/%2.0f\n", this.publisher_split * 100, (1-this.publisher_split) * 100);
        return s;
    }

    public String toString() {
        String s = "";
        s += String.format("Book infomation:\n");
        s += String.format("%s\n", this.book);
        s += String.format("Publisher information:\n");
        s += String.format("%s\n", this.publisher);
        s += String.format("Records:\n");
        s += String.format("%s\n", this.getRecord());
        
        return s;
    }
    
}
