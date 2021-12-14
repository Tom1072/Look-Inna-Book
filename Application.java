import java.util.ArrayList;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Integers;

public class Application {
    private View view;
    private Basket basket;
    private JDBCController JDBC;
    Customer customer;

    public Application(String databaseName, String username, String password) {
        this.customer = null;
        this.JDBC = new JDBCController(databaseName, username, password);
        this.view = new View();
        this.basket = new Basket();
    }

    /** Main Controllers */

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
                    mainExit();
                    break;
                default:
                    view.print("Unknown option\n");
            }
        } while (output != EXIT);
    }

    private void mainExit() {
        view.print("Bye byeee!\n");
    }


    /** Controllers */

    private void customerControl() {
        // Common
        final int BROWSE_BOOK    = 1;
        final int ADD_BOOK       = 2;
        final int REMOVE_BOOK    = 3;
        final int SHOW_BASKET    = 5;
        final int EXIT           = 0;

        // Logged-out user only
        final int LOG_IN         = 9;

        // Logged-in user only
        final int TRACK_ORDERS   = 7;
        final int CHECKOUT       = 8;
        final int LOG_OUT        = 9;

        int option = 0;

        do {
            if (customer == null) {
                // Not logged-in specific
                view.showCustomerScreenNotLoggedIn();
                option = view.getInt();

                switch (option) {
                    case LOG_IN:
                        customerLogin();
                        break;
                    default:
                        break;
                }
            } else {
                // Logged-in specific
                view.showCustomerScreenLoggedIn(customer);
                option = view.getInt();

                switch (option) {
                    case LOG_OUT:
                        customerLogout();
                        break;
                    case CHECKOUT:
                        customerCheckOut();
                        break;
                    case TRACK_ORDERS:
                        customerTrackOrders();
                    default:
                        break;
                }
            }

            switch (option) {
                case LOG_IN:
                    // LOG_IN == LOG_OUT
                case CHECKOUT:
                case TRACK_ORDERS:
                    // Handled on top already
                    break;
                case BROWSE_BOOK:
                    customerBrowseBook();
                    break;
                case ADD_BOOK:
                    customerAddBook();
                    break;
                case REMOVE_BOOK:
                    customerRemoveBook();
                    break;
                case SHOW_BASKET:
                    customerShowBasket();
                    break;
                case EXIT:
                    customerExit();
                    break;
                default:
                    view.print("Unknown option\n");
                    break;
            }
        } while (option != EXIT);
    }
    private void customerLogin() {
        view.print("Enter the name of the customer to log in:\n");
        String name = view.getString();
        customer = JDBC.getCustomer(name);
        if (customer != null) {
            view.print("Logged in successfully!\n");
        } else {
            view.print("Logged in unsuccessful!\n");
        }
    }

    private void customerLogout() {
        customer = null;
        view.print("Logged out unsuccessful!\n");
    }

    private void customerCheckOut() {
        String billingAddress = "";
        String shippingAddress = "";
        int returnCode;

        // Return code from JDBCController.customerCheckout()
        final int SUCCESS                           = 0;
        final int INSUFFICIENT_STOCK                = 1;
        final int SELECT_TUPPLE_FAILED              = 2;
        final int GET_INSERTED_TUPPLE_FAILED        = 3;
        final int INSERT_INTO_ORDER_FAILED          = 4;
        final int INSERT_INTO_CUSTOMERORDER_FAILED  = 5;
        final int INSERT_INTO_ORDERBOOK_FAILED      = 6;

        // Enter information
        view.customerShowBasket(basket);
        view.print("What is your billing address (leave empty to use default)\n");
        billingAddress = view.getString();
        billingAddress = billingAddress != "" ? billingAddress : customer.billing_address;
        view.print("What is your shipping address (leave empty to use default)\n");
        shippingAddress = view.getString();
        shippingAddress = shippingAddress != "" ? shippingAddress : customer.shipping_address;
        returnCode = JDBC.customerCheckout(customer, basket, billingAddress, shippingAddress);
        switch (returnCode) {
            case SUCCESS:
                view.print("Order placed successful!\n");
                basket.clear();
                break;
            case INSUFFICIENT_STOCK:
                view.print("Order canceled because of insufficient stock\n");
                break; 
            case SELECT_TUPPLE_FAILED:
                view.print("FATAL: Select tupple failed\n");
                break;
            case GET_INSERTED_TUPPLE_FAILED:
                view.print("FATAL: Inserted tuple but cannot retrieve it\n");
                break;
            case INSERT_INTO_ORDER_FAILED:
                view.print("FALTAL: Insert into Order failed\n");
                break;
            case INSERT_INTO_CUSTOMERORDER_FAILED:
                view.print("FALTAL: Insert into CustomerOrder failed\n");
                break;
            case INSERT_INTO_ORDERBOOK_FAILED:
                view.print("FALTAL: Insert into OrderBook failed\n");
                break;
            default:
                view.print("FALTAL: Unknown return code\n");
                break;
        }
    }

    private void customerTrackOrders() {
        ArrayList<Integer> order_ids;
        int orderToTrack;
        String trackAnother = "";
        Order order;

        order_ids = JDBC.getCustomerOrders(customer);

        do {
            // Print the order IDs
            view.print("IDs of order placed:\n");
            for (Integer order_id:order_ids) {
                view.print("\t%d\n", order_id);
            }

            view.print("What order do you want to track?\n");
            orderToTrack = view.getInt();

            // Check if orderToTrack is in the order_ids list
            if (!order_ids.contains(orderToTrack)) {
                view.print("Unknown order_id, please choose from the ID list above\n");
                continue;
            }

            // Get that order
            order = JDBC.getOrder(orderToTrack);
            view.print("Information about order %d:\n", orderToTrack);
            view.print("%s\n", order);
            view.print("Do you want to track another order (yes or no)?\n");
            do {
                trackAnother = view.getString();
            } while (!trackAnother.equals("yes") && !trackAnother.equals("no"));

        } while (trackAnother.equals("yes"));
    }

    private void customerAddBook() {
        Book book = null;
        int numOfBooks = -1;
        do {
            view.print("What is the ISBN of the book that you want to order?\n");
            int ISBN = view.getInt();
            book = JDBC.getBook(ISBN);
            if (book == null) {
                view.print("Unknown Book\n");
            }
        } while (book == null);

        do {
            view.print("How many do you want to order? (0 if you change your mind)\n");
            numOfBooks = view.getInt();
            basket.add(book, numOfBooks);
        } while (numOfBooks < 0);
    }

    private void customerRemoveBook() {
        int ISBN = -1;
        int numOfBooks = -1;
        boolean bookExists = false;

        do {
            view.print("What is the ISBN of the book that you want to remove?\n");
            ISBN = view.getInt();
            bookExists = basket.exists(ISBN);
            if (!bookExists) {
                view.print("Book not in basket\n");
            }
        } while (!bookExists);

        do {
            view.print("How many do you want to remove? (0 if you change your mind)\n");
            numOfBooks = view.getInt();
            if (numOfBooks < 0) {
                view.print("Please enter a positive value\n");
            }
            basket.remove(ISBN, numOfBooks);
        } while (numOfBooks < 0);

    }

    private void customerBrowseBook() {
        ArrayList<Book> books = JDBC.getBooks();
        view.customerBrowseBook(books);
    }

    private void customerShowBasket() {
        view.customerShowBasket(basket);
    }

    private void customerExit() {
        view.print("Back to Main View\n");
    }

    /** Owner Controllers */
    private void ownerControl() {

    }

}
