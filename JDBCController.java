import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.Random;

public class JDBCController {
    private static final long MS_PER_DAY = 86400000;

    private Connection connection;
    private Random random;

    public JDBCController(String databaseName, String username, String password) {
        this.random = new Random(System.currentTimeMillis());
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

    public void customerCheckout(Customer customer, Basket basket, String billingAddress, String shippingAddress) {
        int order_id = -1;
        String status = "Order placed";
        Date orderedDate = new Date(System.currentTimeMillis());
        Date estimatedArrivalDate = new Date(System.currentTimeMillis() + ((random.nextInt() % 20 + 5) * MS_PER_DAY));
        String location = "Warehouse";
        

        try {
            // Create the order
            int rowCount;
            PreparedStatement statement;
            ResultSet result;
            String insertQuery = "";
            insertQuery += "insert into TheOrder(billing_address, shipping_address, status, ordered_date, estimated_arrival, location)";
            insertQuery += "    values (?, ?, ?, ?, ?, ?);";

            statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, billingAddress);
            statement.setString(2, shippingAddress);
            statement.setString(3, status);
            statement.setDate(4, orderedDate);
            statement.setDate(5, estimatedArrivalDate);
            statement.setString(6, location);
            rowCount = statement.executeUpdate();
            if (rowCount > 0) {
                System.out.println("insert into Order succeeded");
                result = statement.getGeneratedKeys();
                if (result.next()) {
                    order_id = result.getInt("order_id");
                } else {
                    System.out.println("Cannot get the new inserted tuple");
                }
            } else {
                System.out.println("insert into Order failed");

            }
            // System.out.printf("order_id = %d", order_id);

            // Link the books ordered to the order
            for (BookOrder bookOrder:basket.bookOrders) {
                insertQuery = "insert into OrderBook(ISBN, order_id, unit_ordered) values (?, ?, ?)";
                statement = connection.prepareStatement(insertQuery);
                statement.setInt(1, bookOrder.book.ISBN);
                statement.setInt(2, order_id);
                statement.setInt(3, bookOrder.unit_ordered);
                rowCount = statement.executeUpdate();
                if (rowCount > 0) {
                    System.out.println("insert into OrderBook succeeded");
                } else {
                    System.out.println("insert into OrderBook failed");
                }
            }

            // Link the customer to the order
            insertQuery = "insert into CustomerOrder(order_id, customer_name) values (?, ?)";
            statement = connection.prepareStatement(insertQuery);
            statement.setInt(1, order_id);
            statement.setString(2, customer.name);
            rowCount = statement.executeUpdate();
            if (rowCount > 0) {
                System.out.println("insert into CustomerOrder succeeded");
            } else {
                System.out.println("insert into CustomerOrder failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        // System.out.println(orderedDate);
        // System.out.println(estimatedArrivalDate);

    }
}
