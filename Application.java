import java.util.ArrayList;

public class Application {
    private View view;
    private Basket basket;
    private JDBCController JDBC;
    Customer customer;
    Owner owner;

    private final int MIN_BOOK_THRESHOLD = 50;
    private final int AMOUNT_TO_ORDER = 300;

    public Application(String port, String databaseName, String username, String password) {
        this.customer = null;
        this.JDBC = new JDBCController(port, databaseName, username, password);
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

    /** Customer Controllers */

    private void customerControl() {
        // Common
        final int BROWSE_BOOK    = 1;
        final int ADD_BOOK       = 2;
        final int REMOVE_BOOK    = 3;
        final int SHOW_BASKET    = 5;
        final int EXIT           = 0;

        // Logged-out customer only
        final int LOG_IN         = 9;

        // Logged-in customer only
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
                        customerLogout();
                        continue;
                    case CHECKOUT:
                        customerCheckOut();
                        continue;
                    case TRACK_ORDERS:
                        customerTrackOrders();
                    default:
                        break;
                }
            }

            switch (option) {
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
        if (customer == null) {
            view.print("Enter the name of the customer to log in:\n");
            String name = view.getString();
            customer = JDBC.getCustomer(name);
            if (customer != null) {
                view.print("Customer logged in successfully!\n");
            } else {
                view.print("Customer logged in unsuccessfully!\n");
            }
        }
    }

    private void customerLogout() {
        if (customer != null) {
            customer = null;
            view.print("Customer logged out successfully!\n");
        }
    }

    private void customerCheckOut() {
        String billingAddress = "";
        String shippingAddress = "";
        int returnCode;

        // Return code from JDBCController.customerCheckout()
        final int SUCCESS                           = 0;
        final int INSUFFICIENT_STOCK                = 1;

        // Enter information
        view.customerShowBasket(basket);
        view.print("What is your billing address (leave empty to use default)\n");
        billingAddress = view.getString();
        billingAddress = billingAddress != "" ? billingAddress : customer.billing_address;
        view.print("What is your shipping address (leave empty to use default)\n");
        shippingAddress = view.getString();
        shippingAddress = shippingAddress != "" ? shippingAddress : customer.shipping_address;
        returnCode = JDBC.customerCheckout(customer, basket, billingAddress, shippingAddress);
        if (returnCode == SUCCESS) {
            view.print("Order placed successful!\n");
            basket.clear();
        } else if (returnCode == INSUFFICIENT_STOCK) {
            view.print("Order is canceled because of insufficient stock!\n");
        } else {
            view.print("FATAL: Failed to place order! returnCode = %d\n", returnCode);
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
            book = JDBC.getCustomerBook(ISBN);
            if (book == null) {
                view.print("Unknown Book or Book not collected\n\n");
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
        ArrayList<Book> books = JDBC.getCustomerBooks();
        view.showCustomerBrowseBookMenu();
        view.customerBrowseBook(books);
    }

    private void customerShowBasket() {
        view.customerShowBasket(basket);
    }

    private void customerExit() {
        customerLogout();
        view.print("Back to Main View\n");
    }

    /** Owner Controllers */
    private void ownerControl() {
        // Common
        final int BROWSE_FREE_BOOK              = 1; // Browse only the books that isn't owned by any other owner
        final int EXIT                          = 0;

        // Logged-out owner only
        final int LOG_IN                        = 9;

        // Logged-in owner only
        final int ADD_BOOK                      = 2; 
        final int REMOVE_BOOK                   = 3;
        final int ORDER_BOOK                    = 4;
        final int SHOW_COLLECTION_AND_RECORD    = 5;
        final int LOG_OUT                       = 9;

        int option = 0;

        do {
            if (owner == null) {
                // Not logged-in specific
                view.showOwnerScreenNotLoggedIn();
                option = view.getInt();

                switch (option) {
                    case LOG_IN:
                        ownerLogin();
                        continue;
                    default:
                        break;
                }
            } else {
                // Logged-in specific
                view.showOwnerScreenLoggedIn(owner);
                option = view.getInt();

                switch (option) {
                    case LOG_OUT:
                        ownerLogout();
                        continue;
                    case ADD_BOOK:
                        ownerAddBook();
                        continue;
                    case REMOVE_BOOK:
                        ownerRemoveBook();
                        continue;
                    case ORDER_BOOK:
                        ownerOrderBook();
                        continue;
                    case SHOW_COLLECTION_AND_RECORD:
                        ownerShowCollectionAndRecord();
                        continue;
                    default:
                        break;
                }
            }

            switch (option) {
                case BROWSE_FREE_BOOK:
                    ownerBrowseFreeBook();
                    break;
                case EXIT:
                    ownerExit();
                    break;
                default:
                    view.print("Unknown option\n");
                    break;
            }
        } while (option != EXIT);

    }


    private void ownerLogin() {
        if (owner == null) {
            view.print("Enter the name of the owner to log in:\n");
            String name = view.getString();
            owner = JDBC.getOwner(name);
            if (owner != null) {
                view.print("Owner logged in successfully!\n");
            } else {
                view.print("Owner logged in unsuccessfully!\n");
            }
        }
    }

    private void ownerLogout() {
        if (owner != null) {
            owner = null;
            view.print("Owner logged out successfully!\n");
        }
    }

    private void ownerBrowseFreeBook() {
        ArrayList<Book> books = JDBC.getFreeBooks();
        view.ownerBrowseFreeBook(books);
    }

    private void ownerRemoveBook() {
        int returnCode;
        boolean bookExist;
        int bookToRemove = -1;
        ArrayList<Integer> ISBNs = JDBC.getBooksInCollection(owner);
        
        final int SUCCESS = 0;

        do {
            view.print("Here are the ISBN of books that belong to your collection:\n");
            for (Integer ISBN:ISBNs) {
                view.print("\t%d\n", ISBN);
            }
            view.print("What is the ISBN of the book that you want to remove?\n");
            bookToRemove = view.getInt();
            bookExist = ISBNs.contains(bookToRemove);
            if (!bookExist) {
                view.print("Book not in your collection, please choose from the list above\n");
            } else {
                returnCode = JDBC.removeBookFromCollection(bookToRemove);
                if (returnCode == SUCCESS) {
                    view.print("Book removed from collection successfully\n");
                } else {
                    view.print("Failed to remove book from collection, returnCode = %d\n", returnCode);
                }
            }
        } while (!bookExist);
    }

    private void ownerAddBook() {
        int bookToAdd;
        ArrayList<Integer> freeISBNs = JDBC.getFreeBookISBNs();
        int returnCode;
        double publisher_split;
        final int SUCESS = 0;

        do {
            view.print("Here are the uncollected book ISBNs to choose from:\n");
            for (Integer ISBN:freeISBNs) {
                view.print("\t%d\n", ISBN);
            }
            view.print("What is the ISBN of the book that you want to add to collection?\n");
            bookToAdd = view.getInt();
            if (!freeISBNs.contains(bookToAdd)) {
                view.print("Book not found or already collected by other, please choose from the ISBN list above\n");
                continue;
            } else {
                break;
            }
        } while (true);

        do {
            view.print("What percentage do you want to split the revenue with the Publisher? (enter an integer percentage from 0 to 100)\n");
            publisher_split = (double)(view.getInt()) / (double)100;
        } while (publisher_split < 0 || publisher_split > 1.0);

        returnCode = JDBC.addBookToCollection(bookToAdd, owner, publisher_split);
        if (returnCode == SUCESS) {
            view.print("Book added to collection sucessfully\n");
        } else {
            view.print("FATAL: Book cannot be added to collection, returnCode = %d\n", returnCode);
        }

    }

    private void ownerOrderBook() {
        boolean bookExist;
        ArrayList<Integer> ISBNs = JDBC.getBooksInCollection(owner);
        int bookToOrder;
        int amountToOrder;

        do {
            view.print("Here are the book ISBNs in your collection:\n");
            for (Integer ISBN:ISBNs) {
                view.print("\t%d\n", ISBN);
            }

            view.print("What do you want to order?\n");
            bookToOrder = view.getInt();
            bookExist = ISBNs.contains(bookToOrder);
            if (!bookExist) {
                view.print("Book not found or not in your collection, please enter the value listed\n");
            } else {
                view.print("How many you want to order?\n");
                amountToOrder = view.getInt();
                ownerOrderBook(amountToOrder, bookToOrder);
            }
        } while (!bookExist);

    }

    private void ownerOrderBook(int amountToOrder, int ISBN) {
        int returnCode;
        Collection collection = JDBC.getCollection(ISBN);
        final int SUCCESS = 0;

        returnCode = JDBC.orderBookFromPublisher(amountToOrder, owner, collection);
        if (returnCode == SUCCESS) {
            view.print("Sucessfully ordered %d books with ISBN %d from the publisher\n", amountToOrder, ISBN);
        } else {
            view.print("Failed to order book from publisher, returnCode = %d\n", returnCode);
        }
    }

    private void ownerShowCollectionAndRecord() {
        boolean bookExist;
        ArrayList<Integer> ISBNs;
        int bookToShow;
        Collection collection;

        ISBNs = JDBC.getBooksInCollection(owner);

        do {
            // Print the ISBNs
            view.print("ISBNs of books in the collection:\n");
            for (Integer ISBN:ISBNs) {
                view.print("\t%d\n", ISBN);
            }

            view.print("What book do you want to see more information about?\n");
            bookToShow = view.getInt();

            // Check if bookToShow is in the ISBNs list
            bookExist = ISBNs.contains(bookToShow);
            if (!bookExist) {
                view.print("Unknown ISBN, please choose from the ISBN list above\n");
            } else {
                // Get that book information
                collection = JDBC.getCollection(bookToShow);
                if (collection != null) {
                    view.print("Information about book with ISBN %d:\n", bookToShow);
                    view.print("%s\n", collection);
                } else {
                    view.print("Failed to get collection\n");
                }
            }
        } while (!bookExist);

    }


    private void ownerExit() {
        ownerLogout();
        view.print("Back to Main View\n");
    }

}
