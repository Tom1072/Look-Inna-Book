import java.util.ArrayList;

public class Book {
    public int ISBN;
    public String book_name;
    public String genre;
    public String description;
    public int num_of_pages;
    public double price;
    public String publisher_name;
    public ArrayList<String> authors;

    public Book() {
        this.ISBN = -1;
        this.book_name = "";
        this.genre = "";
        this.description = "";
        this.num_of_pages = -1;
        this.price = -1;
        this.publisher_name = "";
        this.authors = new ArrayList<>();
    }

    public String toString() {
        String s = "";
        s += String.format("ISBN: %d\n", this.ISBN);
        s += String.format("Book name: %s\n", this.book_name);
        s += String.format("Publisher: %s\n", this.publisher_name);
        s += String.format("Genre: %s\n", this.genre);
        s += String.format("Description: %s\n", this.description);
        s += String.format("Number of pages: %d\n", this.num_of_pages);
        s += String.format("Price: $%f\n", this.price);
        s += String.format("Authors: ");

        for (String author:this.authors) {
            s += String.format("%s, ", author);
        }

        s = s.substring(0, s.length()-2); // Remove the last comma
        s += "\n";

        return s;
    }

    public boolean equals(Book anotherBook) {
        return this.ISBN == anotherBook.ISBN;
    }
}
