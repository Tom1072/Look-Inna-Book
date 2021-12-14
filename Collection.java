public class Collection {
    public int ISBN;
    public String owner_name;
    public int unit_in_stock;
    public int unit_sold;
    public double revenue;
    public double publisher_split;
    public Book book;
    public Publisher publisher;

    public Collection() {
        this.unit_in_stock = -1;
        this.unit_sold = -1;
        this.revenue = -1;
        this.publisher_split = -1;
        this.book = null;
        this.publisher = null;
    }
    
}
