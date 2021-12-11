public class Book {
    public String ISBN;
    public String name;
    public String genre;
    public String description;
    public int num_of_pages;
    public double price;
    public Book(String ISBN, String name, String genre, String description, int num_of_pages, double price) {
        this.ISBN = ISBN;
        this.name = name;
        this.genre = genre;
        this.description = description;
        this.num_of_pages = num_of_pages;
        this.price = price;
    }
}
