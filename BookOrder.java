public class BookOrder {
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

    public String toString() {
        String s = "";
        s += String.format("Unit order: %d\n", this.unit_ordered);
        s += String.format("Book info:\n");
        s += String.format("    Name: %s\n", this.book.book_name);

        s += String.format("    Author: ");
        for (String author:this.book.authors) {
            s += String.format("%s, ", author);
        }
        s = s.substring(0, s.length()-2); // Remove the last comma
        s += "\n";

        s += String.format("    Publisher: %s\n", this.book.publisher_name);
        s += String.format("    Genre: %s\n", this.book.genre);
        s += String.format("    Unit Price: $%f\n", this.book.price);
        s += String.format("    Subtotal for this book: $%f\n", this.book.price * this.unit_ordered);
        return s;
    }
    
}
