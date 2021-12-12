import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCController {
    
    private Connection connection;
    
    public JDBCController(String databaseName, String username, String password) {
        try {
            // Please change the following 3 lines as needed
            String url = String.format("jdbc:postgresql://localhost:5432/%s", databaseName);
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Book> getBooks() {
        ArrayList<Book> books = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("select * from Book;");
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    Book book = new Book();
                    book.ISBN = result.getInt("isbn");
                    book.book_name = result.getString("book_name");
                    book.genre = result.getString("genre");
                    book.description = result.getString("description");
                    book.num_of_pages = result.getInt("num_of_pages");
                    book.price = result.getDouble("price");
                    book.publisher_name = result.getString("publisher_name");
                    books.add(book);
                } while (result.next());
            }

            statement = connection.prepareStatement("select * from Author where ISBN=?;");
            for (Book book:books) {
                statement.setInt(1, book.ISBN);
                result = statement.executeQuery();
                if (result.next()) {
                    do {
                        book.authors.add(result.getString("name"));
                    } while (result.next());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Customer getCustomer(String name) {
        Customer customer = null;
        
        try {
            PreparedStatement statement = connection.prepareStatement("select * from Customer where name=?;");
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                customer = new Customer();
                customer.name = result.getString("name");
                customer.billing_address = result.getString("billing_address");
                customer.shipping_address = result.getString("shipping_address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customer;
    }

    public Book getBook(int ISBN) {
        Book book = null;
        try {
            PreparedStatement statement = connection.prepareStatement("select * from Book where ISBN=?;");
            statement.setInt(1, ISBN);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                book = new Book();
                book.ISBN = result.getInt("isbn");
                book.book_name = result.getString("book_name");
                book.genre = result.getString("genre");
                book.description = result.getString("description");
                book.num_of_pages = result.getInt("num_of_pages");
                book.price = result.getDouble("price");
                book.publisher_name = result.getString("publisher_name");
            }

            statement = connection.prepareStatement("select * from Author where ISBN=?;");
            statement.setInt(1, book.ISBN);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    book.authors.add(result.getString("name"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }
}
