import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Application {
    private Connection connection;
    private View view;
    private ArrayList<BookOrder> basket;

    public Application(Connection connection) {
        this.connection = connection;
        view = new View();
        basket = new ArrayList<>();
    }

    public void run() {
        final int EXIT           = 0;
        final int CUSTOMER       = 1;
        final int OWNER          = 2;

        int output = 0;

        do {
            view.showWelcomeScreen();
            output = view.getInt();
            switch (output) {
                case CUSTOMER:
                    customerControl();
                    break;
                case OWNER:
                    ownerControl();
                    break;
                case EXIT:
                    view.print("Bye byeee!\n");
                    break;
                default:
                    view.print("Unknown option\n");
            }
        } while (output != EXIT);
    }

    private void customerControl() {
        // Common
        final int BROWSE_BOOK    = 1;
        final int ADD_BOOK       = 2;
        final int REMOVE_BOOK    = 3;
        final int EDIT_BASKET    = 4;
        final int SHOW_BASKET    = 5;
        final int EXIT           = 0;

        // Logged-out user only
        final int LOG_IN         = 9;

        // Logged-in user only
        final int CHECKOUT       = 8;
        final int LOG_OUT        = 9;

        boolean loggedIn = false;
        Customer customer = null;

        int option = 0;

        do {
            if (!loggedIn) {
                // Not logged-in specific
                view.showCustomerScreenNotLoggedIn();
                option = view.getInt();
            
                switch (option) {
                    case LOG_IN:
                        view.print("Enter the name of the customer to log in:\n");
                        String name = view.getString();
                        customer = customerLogin(name);
                        if (customer != null) {
                            loggedIn = true;
                            view.print("Logged in successfully!\n");
                        } else {
                            view.print("Logged in unsuccessful!\n");
                        }
                        continue;
                    default:
                        break;
                }
            } else {
                // Logged-in specific
                view.showCustomerScreenLoggedIn(customer);
                option = view.getInt();

                switch (option) {
                    case LOG_OUT:
                        customer = null;
                        loggedIn = false;
                        view.print("Logged out unsuccessful!\n");
                        continue;
                    default:
                        break;
                }

            }
            switch (option) {
                case BROWSE_BOOK:
                    ArrayList<Book> books = browseBook();
                    view.customerBrowseBook(books);
                    continue;
                case ADD_BOOK:
                    view.print("What is the ISBN of the book that you want to order?\n");
                    int ISBN = view.getInt();
                    Book book = getBook(ISBN);
                    view.print("How many do you want to order?\n");
                    int unit_ordered = view.getInt();
                    addToBasket(book, unit_ordered);
                    continue;
                case REMOVE_BOOK:
                    view.print("What is the ISBN of the book that you want to remove?\n");
                case SHOW_BASKET:
                    view.customerShowBasket(basket);
                    continue;
                case EXIT:
                    view.print("Back to Main View\n");
                    break;
                default:
                    view.print("Unknown option\n");
                    break;
            }
        } while (option != EXIT);
    }

    private ArrayList<Book> browseBook() {
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

    private Customer customerLogin(String name) {
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

    private Book getBook(int ISBN) {
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

    void addToBasket(Book book, int numOfBook) {
        // Check if book already exist
        boolean bookExist = false;

        for (BookOrder bookOrder:basket) {
            if (bookOrder.book.equals(book)) {
                bookExist = true;
                bookOrder.unit_ordered += numOfBook;
            }
        }

        if (!bookExist) {
            basket.add(new BookOrder(book, numOfBook));
        }

    }

    private void ownerControl() {

    }
}
