public class BookOrder {
    /**
     * Store a book order in a basket for Customer before checkout
     */
    public Book book;
    public int unit_ordered;

    public BookOrder(Book book, int unit_ordered) {
        this.book = book;
        this.unit_ordered = unit_ordered;
    }

    public BookOrder() {
        this.book = null;
        this.unit_ordered = -1;
    }

    public double getRevenue() {
        return (this.book.price * this.unit_ordered);
    }

    public double getExpense() {
        return (this.getRevenue() * this.book.publisher_split);

    }

    public String toString() {
        String s = "";
        s += String.format("Unit order: %d\n", this.unit_ordered);
        s += String.format("Book info:\n");
        s += String.format("    ISBN: %d\n", this.book.ISBN);
        s += String.format("    Name: %s\n", this.book.book_name);
        s += String.format("    Unit Price: $%.2f\n", this.book.price);
        s += String.format("    Subtotal for this book: $%.2f\n", this.book.price * this.unit_ordered);
        return s;
    }
    
}
