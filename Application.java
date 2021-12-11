import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Application {
    private ArrayList<Book> books;
    private ArrayList<Book> customers;
    private ArrayList<Book> owners;
    private Connection connection;

    public Application(Connection connection) {
        this.books = new ArrayList<Book>();
        this.connection = connection;

    }

    private void initBook() {
        // books.add(new Book(""))

    }

    public void run() {
        try {
            PreparedStatement statement = connection.prepareStatement("select * from book;");
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    System.out.println(result.getString("name"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    
}
