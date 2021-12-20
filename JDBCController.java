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

    private final int MIN_BOOK_THRESHOLD = 50;
    private final int AMOUNT_TO_ORDER = 300;

    public JDBCController(String port, String databaseName, String username, String password) {
        this.random = new Random(System.currentTimeMillis());
        try {
            // Please change the following 3 lines as needed
            String url = String.format("jdbc:postgresql://localhost:%s/%s", port, databaseName);
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Browse owned books for customer with filters
     * This is a wrapper around browseBook()
     * @param bookNames
     * @param ISBNs
     * @param authors
     * @param genres
     * @param publishers
     * @return
     */
    public ArrayList<Book> customerBrowseBook(ArrayList<String> bookNames, ArrayList<Integer> ISBNs, ArrayList<String> authors, ArrayList<String> genres, ArrayList<String> publishers) {
        String querry = "select distinct ISBN from Book natural join Author where ISBN in (select ISBN from Collect)";
        return browseBook(querry, bookNames, ISBNs, authors, genres, publishers);
    }

    /**
     * Browse books free books for owner with filters
     * This is a wrapper around browseBook()
     * @param bookNames
     * @param ISBNs
     * @param authors
     * @param genres
     * @param publishers
     * @return
     */
    public ArrayList<Book> ownerBrowseBook(ArrayList<String> bookNames, ArrayList<Integer> ISBNs, ArrayList<String> authors, ArrayList<String> genres, ArrayList<String> publishers) {
        String querry = "select distinct ISBN from Book natural join Author where ISBN not in (select ISBN from Collect)";
        return browseBook(querry, bookNames, ISBNs, authors, genres, publishers);
    }

    /**
     * Browse books with filters
     * @param ISBNs
     * @param authors
     * @param genres
     * @param publishers
     * @return Books that are collected by any owner
     */
    public ArrayList<Book> browseBook(String querry, ArrayList<String> bookNames, ArrayList<Integer> ISBNs, ArrayList<String> authors, ArrayList<String> genres, ArrayList<String> publishers) {
        ArrayList<Book> books = new ArrayList<>();
        String sql = new String(querry);
        try {

            if (bookNames.size() > 0) {
                sql += String.format(" and book_name in (");
                for (String bookName:bookNames) {
                    sql += String.format("'%s',", bookName);
                }
                sql = sql.substring(0, sql.length()-1);
                sql += ")";
            }

            if (ISBNs.size() > 0) {
                sql += String.format(" and ISBN in (");
                for (Integer ISBN:ISBNs) {
                    sql += String.format("%d,", ISBN);
                }
                sql = sql.substring(0, sql.length()-1);
                sql += ")";
            }

            if (authors.size() > 0) {
                sql += String.format(" and author.name in (");
                for (String author:authors) {
                    sql += String.format("'%s',", author);
                }
                sql = sql.substring(0, sql.length()-1);
                sql += ")";
            }

            if (genres.size() > 0) {
                sql += String.format(" and genre in (");
                for (String genre:genres) {
                    sql += String.format("'%s',", genre);
                }
                sql = sql.substring(0, sql.length()-1);
                sql += ")";
            }

            if (publishers.size() > 0) {
                sql += String.format(" and publisher_name in (");
                for (String publisher:publishers) {
                    sql += String.format("'%s',", publisher);
                }
                sql = sql.substring(0, sql.length()-1);
                sql += ")";
            }
            sql += "order by ISBN;";
            System.out.printf("JDBC: %s\n", sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    books.add(getBook(result.getInt("ISBN")));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /**
     * Find the customer with a given name
     * @param name
     * @return
     */
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
                customer.balance = result.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customer;
    }

    /**
     * Find the Book with the given ISBN
     * @param ISBN
     * @return
     */
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

            // Get the author name
            statement = connection.prepareStatement("select name from Author where ISBN=?;");
            statement.setInt(1, book.ISBN);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    book.authors.add(result.getString("name"));
                } while (result.next());
            }

            // Get the owner that collected this book and the publisher revenue split ratio
            statement = connection.prepareStatement("select owner_name, publisher_split from Collect where ISBN=?;");
            statement.setInt(1, book.ISBN);
            result = statement.executeQuery();
            if (result.next()) {
                book.owner_name = result.getString("owner_name");
                book.publisher_split = result.getDouble("publisher_split");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * Checkout the "basket" under "customer"
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
        final int INSUFFICIENT_FUND                 = 2;
        final int SELECT_TUPPLE_FAILED              = 3;
        final int GET_INSERTED_TUPPLE_FAILED        = 4;
        final int INSERT_INTO_ORDER_FAILED          = 5;
        final int INSERT_INTO_CUSTOMERORDER_FAILED  = 6;
        final int INSERT_INTO_ORDERBOOK_FAILED      = 7;
        final int UPDATE_OWNER_BALANCE_FAILED       = 8;
        final int UPDATE_CUSTOMER_BALANCE_FAILED    = 9;
        final int GET_OWNER_BALANCE_FAILED          = 10;

        int order_id = -1;
        String status = "Order placed";
        Date orderedDate = new Date(System.currentTimeMillis());
        Date estimatedArrivalDate = new Date(System.currentTimeMillis() + ((random.nextInt() % 20 + 5) * MS_PER_DAY));
        String location = "Warehouse";
        
        try {
            // Check if every books are in stock
            sql = "select unit_in_stock from Collect where ISBN=?;";
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

            sql = "select balance from Customer where name=?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, customer.name);
            result = statement.executeQuery();
            if (result.next()) {
                if (result.getDouble("balance") < basket.getTotalRevenue()) {
                    return INSUFFICIENT_FUND;
                }
            } else {
                return GET_OWNER_BALANCE_FAILED;
            }

            // Update the book sale record in Collect
            sql = "update Collect set unit_in_stock=unit_in_stock-?, unit_sold=unit_sold+?, revenue=revenue+? where ISBN=?;";
            statement = connection.prepareStatement(sql);
            for (BookOrder bookOrder:basket.bookOrders) {
                statement.setInt(1, bookOrder.unit_ordered);
                statement.setInt(2, bookOrder.unit_ordered);
                statement.setDouble(3, bookOrder.getRevenue());
                statement.setInt(4, bookOrder.book.ISBN);
                statement.addBatch();
            }
            returnCodes = statement.executeBatch();
            if (!isGoodReturnCodes(returnCodes)) {
                return INSUFFICIENT_STOCK;
            }

            // Send the revenue to the Owner balance
            sql = "update Owner set balance=balance+? where name=?";
            statement = connection.prepareStatement(sql);
            for (int i=0; i<basket.bookOrders.size(); i++) {
                BookOrder bookOrder = basket.getBookOrderAt(i);
                statement.setDouble(1, bookOrder.getRevenue());
                statement.setString(2, bookOrder.book.owner_name);
                statement.addBatch();
            }

            returnCodes = statement.executeBatch();
            if (!isGoodReturnCodes(returnCodes)) {
                return UPDATE_OWNER_BALANCE_FAILED;
            }

            // Deduce the cost from customer balance
            sql = "update Customer set balance=balance-? where name=?";
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, basket.getTotalRevenue());
            statement.setString(2, customer.name);
            statement.addBatch();
            returnCodes = statement.executeBatch();
            if (!isGoodReturnCodes(returnCodes)) {
                return UPDATE_CUSTOMER_BALANCE_FAILED;
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
            sql = "insert into OrderBook(ISBN, order_id, unit_ordered) values (?, ?, ?)";
            statement = connection.prepareStatement(sql);
            for (BookOrder bookOrder:basket.bookOrders) {
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

        handleAutomaticOrdering(basket);

        return SUCCESS;
    }

    /**
     * Handle automatic ordering when customer succesfully checked out
     * @param basket
     */
    private void handleAutomaticOrdering(Basket basket) {
        for (BookOrder bookOrder:basket.bookOrders) {
            Collection collection = getCollection(bookOrder.book.ISBN);;
            if (collection.unit_in_stock < MIN_BOOK_THRESHOLD) {
                Owner owner = getOwner(collection.owner_name);
                orderBookFromPublisher(AMOUNT_TO_ORDER, owner, collection);
                System.out.printf("JDBC: Automatically order %d books with ISBN %d for owner %s\n",
                                AMOUNT_TO_ORDER, bookOrder.book.ISBN, owner.name);
            }
        }
    }

    /**
     * Find the Order IDs of a Customer
     * @param customer
     * @return
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

    /**
     * Get the order with given order_id
     * @param order_id
     * @return
     */
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

    /**
     * Get the Owner with the given name 
     * @param name
     * @return
     */
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
                owner.balance = result.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return owner;
        
    }

    /**
     * Get the ISBN of books that are not owned by anyone
     * @return the ISBNs of unowned book
     */
    public ArrayList<Integer> getFreeBookISBNs() {
        ArrayList<Integer> ISBNs = new ArrayList<>();
        String sql;

        try {
            sql = "select ISBN from Book where ISBN not in (select ISBN from Collect) order by ISBN;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    ISBNs.add(result.getInt("ISBN"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ISBNs;
    }

    /**
     * Get the books that are currently not owned by anyone
     * @return the Book objects that are not owned by anyone
     */
    public ArrayList<Book> getFreeBooks() {
        ArrayList<Book> books = new ArrayList<>();
        ArrayList<Integer> ISBNs = new ArrayList<>();
        ISBNs = getFreeBookISBNs();
        for (Integer ISBN:ISBNs) {
            books.add(getBook(ISBN));
        }
        return books;
    }

    /**
     * Get the name of books that are in some owner's collection
     * @return the names of owned book
     */
    public ArrayList<String> getOwnedBookNames() {
        ArrayList<String> bookNames = new ArrayList<>();
        String sql;

        try {
            sql = "select book_name from Book where ISBN in (select ISBN from Collect) order by book_name;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    bookNames.add(result.getString("book_name"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookNames;
    }

    /**
     * Get the ISBN of books that are in some owner's collection
     * @return the ISBNs of owned book
     */
    public ArrayList<Integer> getOwnedBookISBNs() {
        ArrayList<Integer> ISBNs = new ArrayList<>();
        String sql;

        try {
            sql = "select ISBN from Book where ISBN in (select ISBN from Collect) order by ISBN;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    ISBNs.add(result.getInt("ISBN"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ISBNs;
    }

    /**
     * Get the books that are in some owner's collection
     * @return the Book objects that are owned by some owner
     */
    public ArrayList<Book> getOwnedBooks() {
        ArrayList<Book> books = new ArrayList<>();
        ArrayList<Integer> ISBNs = new ArrayList<>();
        ISBNs = getOwnedBookISBNs();
        for (Integer ISBN:ISBNs) {
            books.add(getBook(ISBN));
        }
        return books;
    }

    /**
     * Get all books in an owner collection (no filter)
     * This is a wrapper around getBooksInCollection
     * @param owner
     * @return
     */
    public ArrayList<Integer> getAllBooksInCollection(Owner owner) {
        return getBooksInCollection(owner, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
    }

    /**
     * Get all collections with filter
     * This is a wrapper around getBooksInCollection() and getCollection()
     * @param owner
     * @param genres
     * @param authors
     * @param publishers
     * @return
     */
    public ArrayList<Collection> getCollections(Owner owner, ArrayList<String> genres, ArrayList<String> authors, ArrayList<String> publishers) {
        ArrayList<Integer> ISBNs = getBooksInCollection(owner, genres, authors, publishers);
        ArrayList<Collection> collections = new ArrayList<>();
        for (Integer ISBN:ISBNs) {
            collections.add(getCollection(ISBN));
        }
        return collections;
    }

    /**
     * Get the book ISBNs in the collection of an owner
     * @param owner
     * @return An ArrayList of Book ISBN that belongs to owner
     */
    public ArrayList<Integer> getBooksInCollection(Owner owner, ArrayList<String> genres, ArrayList<String> authors, ArrayList<String> publishers) {
        String querry;
        ArrayList<Book> ownerBooks;
        ArrayList<Integer> ownerBookISBNs = new ArrayList<>();

        querry = String.format("select distinct ISBN from (((Owner join Collect on Owner.name=Collect.owner_name) join Book using (ISBN)) join Author using (ISBN)) where Owner.name='%s'", owner.name);

        ownerBooks = browseBook(querry, new ArrayList<String>(), new ArrayList<Integer>(), authors, genres, publishers);
        for (Book book: ownerBooks) {
            ownerBookISBNs.add(book.ISBN);
        }
        return ownerBookISBNs;
    }

    /**
     * Get all available author names
     * @return authors ArrayList of String
     */
    public ArrayList<String> getAuthors() {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        ArrayList<String> authors = new ArrayList<>();

        sql = "select distinct name from Author order by name;";
        try {
            statement = connection.prepareStatement(sql);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    authors.add(result.getString("name"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return authors;
    }

    /**
     * Get all available genres
     * @return genres ArrayList of String
     */
    public ArrayList<String> getGenres() {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        ArrayList<String> genres = new ArrayList<>();

        sql = "select distinct genre from Book order by genre;";
        try {
            statement = connection.prepareStatement(sql);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    genres.add(result.getString("genre"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    /**
     * Get all available publisher names
     * @return publishers ArrayList of String
     */
    public ArrayList<String> getPublishers() {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        ArrayList<String> publishers = new ArrayList<>();

        sql = "select name from Publisher order by name;";
        try {
            statement = connection.prepareStatement(sql);
            result = statement.executeQuery();
            if (result.next()) {
                do {
                    publishers.add(result.getString("name"));
                } while (result.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return publishers;
    }


    /**
     * 
     * @param name
     * @return Get the Publisher with the given name
     */
    private Publisher getPublisher(String name) {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        Publisher publisher = null;

        try {
            // Get the information from Collect table
            sql = "select * from Publisher where name=?;";
            statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            result = statement.executeQuery();
            if (result.next()) {
                publisher = new Publisher();
                publisher.name = result.getString("name");
                publisher.email = result.getString("email");
                publisher.bank_account = result.getString("bank_account");
                // publisher.balance = result.getDouble("balance");
                publisher.address = result.getString("address");
                publisher.phone_number = result.getString("phone_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return publisher;
    }

    /**
     * Get the collection of a book with the given ISBN. The collection includes
     *      Owner name
     *      Sale information
     *      Publisher revenue split information
     * @param ISBN
     * @return Collection object if suceed, null if failed
     */
    public Collection getCollection(int ISBN) {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        Collection collection = null;
        
        try {
            // Get the information from Collect table
            sql = "select * from Collect where ISBN=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, ISBN);
            result = statement.executeQuery();
            if (result.next()) {
                collection = new Collection();

                collection.ISBN = result.getInt("ISBN");
                collection.owner_name = result.getString("owner_name");
                collection.unit_in_stock = result.getInt("unit_in_stock");
                collection.unit_sold = result.getInt("unit_sold");
                collection.revenue = result.getDouble("revenue");
                collection.expense = result.getDouble("expense");
                collection.publisher_split = result.getDouble("publisher_split");
                collection.book = getBook(ISBN);
                collection.publisher = getPublisher(collection.book.publisher_name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return collection;
    }

    /**
     * Add the book with ISBN to the owner's collection and order MIN_BOOK_THRESHOLD books from owner
     * @param ISBN
     * @param owner
     * @return the returnCode, 0 for SUCCESS, FAILURE otherwise
     */
    public int addBookToCollection(int ISBN, Owner owner, double publisher_split, double price) {
        PreparedStatement statement;
        String sql;
        final int SUCCESS = 0;
        final int INSERT_INTO_COLLECT_FAILED = 1;
        final int UPDATE_BOOK_PRICE_FAILED = 2;
        final int ORDER_BOOK_FROM_PUBLISHER_FAILED = 3;
        int unit_in_stock = 0;
        int unit_sold = 0;
        double revenue = 0;
        double expense = 0;


        try {
            // Add new book to collection
            sql = "insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) ";
            sql += "values (?, ?, ?, ?, ?, ?, ?);";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, ISBN);
            statement.setString(2, owner.name);
            statement.setInt(3, unit_in_stock);
            statement.setInt(4, unit_sold);
            statement.setDouble(5, revenue);
            statement.setDouble(6, expense);
            statement.setDouble(7, publisher_split);
            if (statement.executeUpdate() == 0) {
                return INSERT_INTO_COLLECT_FAILED;
            }
            
            // Update the book price
            sql = "update Book set price=? where ISBN=?";
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, price);
            statement.setInt(2, ISBN);
            if (statement.executeUpdate() == 0) {
                return UPDATE_BOOK_PRICE_FAILED;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Order a starting number of books from publisher
        if (orderBookFromPublisher(AMOUNT_TO_ORDER, owner, getCollection(ISBN)) == SUCCESS) {
            return SUCCESS;
        } else {
            return ORDER_BOOK_FROM_PUBLISHER_FAILED;
        }

    }

    /**
     * Remove a book with given ISBN from the owner collection
     * Afterward the book is free to be added to any other owner's collection
     * @param ISBN
     * @return 0 if suceed, > 0 otherwise
     */
    public int removeBookFromCollection(int ISBN) {
        PreparedStatement statement;
        String sql;

        final int SUCCESS = 0;
        final int DELETE_FROM_COLLECT_FAILED = 1;

        try {
            sql = "delete from Collect where ISBN=?;";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, ISBN);
            if (isGoodReturnCode(statement.executeUpdate())) {
                return SUCCESS;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DELETE_FROM_COLLECT_FAILED;
    }

    /**
     * Email the publisher
     * Order more books which belongs to the given collection for the given owner
     * @param amountToOrder
     * @param owner
     * @param collection
     * @return
     */
    public int orderBookFromPublisher(int amountToOrder, Owner owner, Collection collection) {
        PreparedStatement statement;
        ResultSet result;
        String sql;
        double orderCost = collection.getPublisherSplitPerBook() * amountToOrder;
        final int SUCESSS = 0;
        final int INSUFFICIENT_FUND = 1;
        final int QUERY_FAILED = 2;
        final int UPDATE_OWNER_BALANCE_FAILED = 3;
        final int UPDATE_PUBLISHER_BALANCE_FAILED = 4;
        final int UPDATE_COLLECTION_FAILED = 5;

        try {
            // Check if owner has sufficient fund
            sql = "select balance from Owner where name=?;";
            statement = connection.prepareStatement(sql);
            statement.setString(1, owner.name);
            result = statement.executeQuery();
            if (result.next()) {
                if (result.getDouble("balance") < orderCost) {
                    return INSUFFICIENT_FUND;
                } 
            } else {
                return QUERY_FAILED;
            }

            // Withdraw from owner
            sql = "update Owner set balance=balance-? where name=?;";
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, orderCost);
            statement.setString(2, owner.name);
            if (statement.executeUpdate() == 0) {
                return UPDATE_OWNER_BALANCE_FAILED;
            }

            // Pay the publisher
            sql = "update Publisher set balance=balance+? where name=?;";
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, orderCost);
            statement.setString(2, collection.publisher.name);
            if (statement.executeUpdate() == 0) {
                return UPDATE_PUBLISHER_BALANCE_FAILED;
            }

            // Update the collection stock and expense
            sql = "update Collect set unit_in_stock=unit_in_stock+?, expense=expense+? where ISBN=?;";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, amountToOrder);
            statement.setDouble(2, orderCost);
            statement.setInt(3, collection.book.ISBN);
            if (statement.executeUpdate() == 0) {
                return UPDATE_COLLECTION_FAILED;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return SUCESSS;
    }

    /**
     * Check if all returnCodes are good or not
     * @param returnCodes
     * @return true if all return Codes are good, false otherwise
     */
    private boolean isGoodReturnCodes(int[] returnCodes) {
        for (int i=0; i<returnCodes.length; i++) {
            if (!isGoodReturnCode(returnCodes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the return code is good or not
     * @param returnCode
     * @return true if the return code is good, false otherwise
     */
    private boolean isGoodReturnCode(int returnCode) {
        return returnCode > 0;
    }

}
