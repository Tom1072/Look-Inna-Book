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

    public ArrayList<Book> getOwnedBooks() {
        ArrayList<Book> books = new ArrayList<>();
        String sql;
        try {
            sql = "select * from Book where ISBN in (select ISBN from Collect);";
            PreparedStatement statement = connection.prepareStatement(sql);
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
            } else {
                return null;
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

    /**
     * Checkout the "basket" under "customer", assume that all orders in basket is in stock
     * @param customer
     * @param basket
     * @param billingAddress
     * @param shippingAddress
     */
    public int customerCheckout(Customer customer, Basket basket, String billingAddress, String shippingAddress) {
        int[] returnCodes;
        int rowCount;
        PreparedStatement statement;
        ResultSet result;
        String sql;

        // Return code
        final int SUCCESS                           = 0;
        final int INSUFFICIENT_STOCK                = 1;
        final int SELECT_TUPPLE_FAILED              = 2;
        final int GET_INSERTED_TUPPLE_FAILED        = 3;
        final int INSERT_INTO_ORDER_FAILED          = 4;
        final int INSERT_INTO_CUSTOMERORDER_FAILED  = 5;
        final int INSERT_INTO_ORDERBOOK_FAILED      = 6;

        int order_id = -1;
        String status = "Order placed";
        Date orderedDate = new Date(System.currentTimeMillis());
        Date estimatedArrivalDate = new Date(System.currentTimeMillis() + ((random.nextInt() % 20 + 5) * MS_PER_DAY));
        String location = "Warehouse";
        
        try {
            // Check if every books are in stock
            sql = "select unit_in_stock from Collect where ISBN=?";
            statement = connection.prepareStatement(sql);
            for (BookOrder bookOrder:basket.bookOrders) {
                statement.setInt(1, bookOrder.book.ISBN);
                result = statement.executeQuery();
                if (result.next()) {
                    if (result.getInt("unit_in_stock") < bookOrder.unit_ordered) {
                        return INSUFFICIENT_STOCK;
                    }
                } else {
                    return SELECT_TUPPLE_FAILED;
                }
            }

            // Decrement the unit_in_stock of each book in owner collection
            sql = "update Collect set unit_in_stock=unit_in_stock-? where ISBN=?";
            statement = connection.prepareStatement(sql);
            for (BookOrder bookOrder:basket.bookOrders) {
                statement.setInt(1, bookOrder.unit_ordered);
                statement.setInt(2, bookOrder.book.ISBN);
                statement.addBatch();
            }
            returnCodes = statement.executeBatch();
            if (!isGoodReturnCodes(returnCodes)) {
                return INSUFFICIENT_STOCK;
            }

            // Create the order
            sql = "insert into TheOrder(billing_address, shipping_address, status, ordered_date, estimated_arrival, location)";
            sql += "    values (?, ?, ?, ?, ?, ?);";

            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, billingAddress);
            statement.setString(2, shippingAddress);
            statement.setString(3, status);
            statement.setDate(4, orderedDate);
            statement.setDate(5, estimatedArrivalDate);
            statement.setString(6, location);
            rowCount = statement.executeUpdate();
            if (rowCount > 0) {
                result = statement.getGeneratedKeys();
                if (result.next()) {
                    order_id = result.getInt("order_id");
                } else {
                    // System.out.println("Cannot get the new inserted tuple");
                    return GET_INSERTED_TUPPLE_FAILED;
                }
            } else {
                // System.out.println("insert into Order failed");
                return INSERT_INTO_ORDER_FAILED;
            }
            // System.out.printf("order_id = %d", order_id);

            // Link the customer to the order
            sql = "insert into CustomerOrder(order_id, customer_name) values (?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, order_id);
            statement.setString(2, customer.name);
            rowCount = statement.executeUpdate();
            if (rowCount == 0) {
                // System.out.println("insert into CustomerOrder failed");
                return INSERT_INTO_CUSTOMERORDER_FAILED;
            }

            // Link the books ordered to the order
            for (BookOrder bookOrder:basket.bookOrders) {
                sql = "insert into OrderBook(ISBN, order_id, unit_ordered) values (?, ?, ?)";
                statement = connection.prepareStatement(sql);
                statement.setInt(1, bookOrder.book.ISBN);
                statement.setInt(2, order_id);
                statement.setInt(3, bookOrder.unit_ordered);
                statement.addBatch();
            }
            returnCodes = statement.executeBatch();
            if (!isGoodReturnCodes(returnCodes)) {
                return INSERT_INTO_ORDERBOOK_FAILED;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    /**
     * 
     * @param customer
     * @return the order_ids of that customer
     */
    public ArrayList<Integer> getCustomerOrders(Customer customer) {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        ArrayList<Integer> order_ids = new ArrayList<>();

        sql = "select order_id from CustomerOrder where customer_name=?";

        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, customer.name);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    order_ids.add(result.getInt("order_id"));
                } while (result.next());
            }
                
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return order_ids;
    }

    public Order getOrder(int order_id) {
        Order order = null;
        PreparedStatement statement;
        ResultSet result;
        String sql;
        ArrayList<Integer> ISBNs = new ArrayList<>();
        ArrayList<Integer> unit_ordereds = new ArrayList<>();


        try {
            // Get the order itself in TheOrder
            sql = "select * from TheOrder where order_id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, order_id);
            result = statement.executeQuery();
            if (result.next()) {
                order = new Order();
                order.order_id = result.getInt("order_id");
                order.billing_address = result.getString("billing_address");
                order.shipping_address = result.getString("shipping_address");
                order.status = result.getString("status");
                order.ordered_date = result.getDate("ordered_date").toString();
                order.estimated_arrival = result.getDate("estimated_arrival").toString();
                order.location = result.getString("location");
            }

            // Get the ISBNs and unit_ordereds that is ordered in OrderBook
            sql = "select * from OrderBook where order_id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, order_id);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    ISBNs.add(result.getInt("ISBN"));
                    unit_ordereds.add(result.getInt("unit_ordered"));
                } while (result.next());
            }

            // Get the Books that has the ISBN in the order
            for (int i=0; i<ISBNs.size(); i++) {
                Book book = getBook(ISBNs.get(i));
                order.bookOrders.add(book, unit_ordereds.get(i));
            }
                
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return order;
    }

    public Owner getOwner(String name) {
        Owner owner = null;
        
        try {
            PreparedStatement statement = connection.prepareStatement("select * from Owner where name=?;");
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                owner = new Owner();
                owner.name = result.getString("name");
                owner.bank_account = result.getString("bank_account");
                owner.email = result.getString("email");
                owner.phone_number = result.getString("phone_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return owner;
        
    }

    public ArrayList<Book> getFreeBooks() {
        ArrayList<Book> books = new ArrayList<>();
        String sql;
        try {
            sql = "select * from Book where ISBN not in (select ISBN from Collect);";
            PreparedStatement statement = connection.prepareStatement(sql);
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

    private boolean isGoodReturnCodes(int[] returnCodes) {
        boolean good = true;
        for (int i=0; i<returnCodes.length; i++) {
            if (returnCodes[i] == 0) {
                good = false;
                break;
            }
        }
        return good;
    }

}
