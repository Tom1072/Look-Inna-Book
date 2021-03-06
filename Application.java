import java.util.ArrayList;

public class Application {
    private View view;
    private Basket basket;
    private JDBCController JDBC;
    Customer customer;
    Owner owner;

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
        final int SHOW_BASKET    = 4;
        final int EXIT           = 0;

        // Logged-out customer only
        final int LOG_IN         = 5;

        // Logged-in customer only
        final int TRACK_ORDERS   = 5;
        final int CHECKOUT       = 6;
        final int LOG_OUT        = 7;

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

    /**
     * Login to a customer
     */
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

    /**
     * Logout of a customer
     */
    private void customerLogout() {
        if (customer != null) {
            customer = null;
            view.print("Customer logged out successfully!\n");
        }
    }

    /**
     * Check out a basket for a customer
     */
    private void customerCheckOut() {
        String billingAddress = "";
        String shippingAddress = "";
        int returnCode;

        // Return code from JDBCController.customerCheckout()
        final int SUCCESS                           = 0;
        final int INSUFFICIENT_STOCK                = 1;
        final int INSUFFICIENT_FUND                 = 2;

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
            customer = JDBC.getCustomer(customer.name); // Update customer information (balance)
        } else if (returnCode == INSUFFICIENT_STOCK) {
            view.print("Order is canceled because of insufficient stock!\n");
        } else if (returnCode == INSUFFICIENT_FUND) {
            view.print("Order is canceled because of insufficient fund from customer!\n");
        } else {
            view.print("FATAL: Failed to place order! returnCode = %d\n", returnCode);
        }
    }

    /**
     * Track a customer order
     */
    private void customerTrackOrders() {
        ArrayList<Integer> order_ids;
        int orderToTrack;
        String trackAnother = "";
        Order order;

        order_ids = JDBC.getCustomerOrders(customer);
        if (order_ids.size() == 0) {
            view.print("There's no order to track\n");
            return;
        }

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

    /**
     * Add a book order to the basket
     */
    private void customerAddBook() {
        ArrayList<Integer> ISBNs;
        Book book = null;
        int numOfBooks = -1;
        boolean bookExist = false;

        do {
            ISBNs = JDBC.getOwnedBookISBNs();
            view.print("List of Book ISBNs to add:\n");
            for (Integer ISBN:ISBNs) {
                Book b = JDBC.getBook(ISBN);
                view.print("\tISBN: %d\n", ISBN);
                view.print("\tname: %s\n", b.book_name);
                view.print("\tprice per book: %.2f\n", b.price);
                view.print("\n", ISBN);
            }
            view.print("What is the ISBN of the book that you want to order? (0 to exit)\n");
            int ISBN = view.getInt();
            bookExist = ISBNs.contains(ISBN);

            if (!bookExist && ISBN > 0) {
                view.print("Unknown Book or Book not collected, please choose from the list above\n");
            } else if (ISBN == 0) {
                return;
            } else {
                book = JDBC.getBook(ISBN);
                break;
            }
        } while (!bookExist);

        do {
            view.print("How many do you want to order? (0 if you change your mind)\n");
            numOfBooks = view.getInt();
        } while (numOfBooks < 0);
        basket.add(book, numOfBooks);
    }

    /**
     * Remove a book order from the basket
     */
    private void customerRemoveBook() {
        int ISBN = -1;
        int numOfBooks = -1;
        boolean bookExists = false;

        do {
            view.customerShowBasket(basket);
            view.print("What is the ISBN of the book that you want to remove? (0 to exit)\n");
            ISBN = view.getInt();
            bookExists = basket.exists(ISBN);
            if (!bookExists && ISBN > 0) {
                view.print("Book not in basket\n");
            } else if (ISBN == 0) {
                return;
            } else {
                break;
            }
        } while (true);

        do {
            view.print("How many do you want to remove? (0 if you change your mind)\n");
            numOfBooks = view.getInt();
            if (numOfBooks < 0) {
                view.print("Please enter a positive value\n");
            }
            basket.remove(ISBN, numOfBooks);
        } while (numOfBooks < 0);

    }

    /**
     * Browse for customer books (books that are collected by an owner)
     */
    private void customerBrowseBook() {
        browseBook(true);
    }

    /**
     * Show all book orders in the basket
     */
    private void customerShowBasket() {
        view.customerShowBasket(basket);
    }

    /**
     * Exit from customer view
     */
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
        final int LOG_IN                        = 2;

        // Logged-in owner only
        final int ADD_BOOK                      = 2; 
        final int REMOVE_BOOK                   = 3;
        final int ORDER_BOOK                    = 4;
        final int SHOW_COLLECTION               = 5;
        final int SHOW_RECORD                   = 6;
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
                    case SHOW_COLLECTION:
                        ownerShowCollection();
                        continue;
                    case SHOW_RECORD:
                        ownerShowRecord();
                        continue;
                    default:
                        break;
                }
            }

            switch (option) {
                case BROWSE_FREE_BOOK:
                    ownerBrowseBook();
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

    /**
     * Browse for unowned books for owner to add to collection
     */
    private void ownerBrowseBook() {
        browseBook(false);
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

    /**
     * Logout from an owner
     */
    private void ownerLogout() {
        if (owner != null) {
            owner = null;
            view.print("Owner logged out successfully!\n");
        }
    }

    /**
     * Remove a book from an owner's collection
     */
    private void ownerRemoveBook() {
        int returnCode;
        boolean bookExist;
        int bookToRemove = -1;
        ArrayList<Integer> ISBNs = JDBC.getAllBooksInCollection(owner);
        
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

    /**
     * Add a book to an owner collection and order a fixed about from the publisher
     */
    private void ownerAddBook() {
        int bookToAdd;
        ArrayList<Integer> freeISBNs = JDBC.getFreeBookISBNs();
        int returnCode;
        double publisher_split;
        double price;
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

        view.print("What price do you want to set for the book?\n");
        price = view.getDouble(0, Double.MAX_VALUE);

        view.print("What percentage do you want to split the revenue with the Publisher? (enter a ratio from 0 to 1)\n");
        publisher_split = view.getDouble(0, 1.0);

        returnCode = JDBC.addBookToCollection(bookToAdd, owner, publisher_split, price);
        if (returnCode == SUCESS) {
            view.print("Book added to collection sucessfully\n");
            owner = JDBC.getOwner(owner.name); // Update owner information (balance)
        } else {
            view.print("FATAL: Book cannot be added to collection, returnCode = %d\n", returnCode);
        }

    }

    /**
     * Order books from Publisher (only for this owner's collected book)
     * Wrapper for ownerOrderBook(int, int)
     */
    private void ownerOrderBook() {
        boolean bookExist;
        ArrayList<Integer> ISBNs = JDBC.getAllBooksInCollection(owner);
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

    /**
     * Order amountToOrder books of ISBN from the publisher
     * @param amountToOrder
     * @param ISBN
     */
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

    /**
     * Show a books in an owner's collection
     */
    private void ownerShowCollection() {
        ArrayList<Integer> ISBNs;
        ArrayList<String> genreFilter = new ArrayList<>();
        ArrayList<String> authorFilter = new ArrayList<>();
        ArrayList<String> publisherFilter = new ArrayList<>();
        Collection collection;

        ISBNs = JDBC.getBooksInCollection(owner, genreFilter, authorFilter, publisherFilter);

        for (Integer ISBN:ISBNs) {
            // Get that book information
            collection = JDBC.getCollection(ISBN);
            if (collection != null) {
                view.print("%s\n", collection.showBook());
            } else {
                view.print("Failed to get collection\n");
            }
        }
    }

    /**
     * Show sale record for an owner's collection
     */
    private void ownerShowRecord() {
        int option;
        int indexChoice;
        ArrayList<String> availableAuthors = JDBC.getAuthors();
        ArrayList<String> availableGenre = JDBC.getGenres();
        ArrayList<String> availablePublisher = JDBC.getPublishers();
        ArrayList<Collection> collections;

        ArrayList<String> genreFilter = new ArrayList<>();
        ArrayList<String> authorFilter = new ArrayList<>();
        ArrayList<String> publisherFilter = new ArrayList<>();

        final int SHOW_RECORDS          = 1;
        final int SHOW_FILTERS          = 2;
        final int CLEAR_FILTERS         = 3;
        final int GENRE_FILTER_ADD      = 4;
        final int AUTHOR_FILTER_ADD     = 5;
        final int PUBLISHER_FILTER_ADD  = 6;
        final int EXIT                  = 0;

        do {
            view.showOwnerRecordMenu();
            option = view.getInt();

            switch (option) {
                case SHOW_RECORDS:
                    collections = JDBC.getCollections(owner, genreFilter, authorFilter, publisherFilter);
                    view.showCollections(collections);
                    break;
                case SHOW_FILTERS:
                    view.print("Genre Filter: %s\n", genreFilter.toString());
                    view.print("Author Filter: %s\n", authorFilter.toString());
                    view.print("Publisher Filter: %s\n", publisherFilter.toString());
                    break;
                case CLEAR_FILTERS:
                    genreFilter.clear();
                    authorFilter.clear();
                    publisherFilter.clear();
                    view.print("All filter cleared\n");
                    break;

                case GENRE_FILTER_ADD:
                    view.print("Available Genres:\n");
                    for (int i=0; i<availableGenre.size(); i++) {
                        view.print("(%d) %s\n", i, availableGenre.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availableGenre.size() - 1);
                    if (!genreFilter.contains(availableGenre.get(indexChoice))) {
                        genreFilter.add(availableGenre.get(indexChoice));
                    } else {
                        view.print("Genre %s already existed in the Genre Filter\n", availableGenre.get(indexChoice));
                    }
                    break;

                case AUTHOR_FILTER_ADD:
                    view.print("Available Authors:\n");
                    for (int i=0; i<availableAuthors.size(); i++) {
                        view.print("(%d) %s\n", i, availableAuthors.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availableAuthors.size() - 1);
                    if (!authorFilter.contains(availableAuthors.get(indexChoice))) {
                        authorFilter.add(availableAuthors.get(indexChoice));
                    } else {
                        view.print("Author %s already existed in the Author Filter\n", availableAuthors.get(indexChoice));
                    }
                    break;

                case PUBLISHER_FILTER_ADD:
                    view.print("Available Publishers:\n");
                    for (int i=0; i<availablePublisher.size(); i++) {
                        view.print("(%d) %s\n", i, availablePublisher.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availablePublisher.size() - 1);
                    if (!publisherFilter.contains(availablePublisher.get(indexChoice))) {
                        publisherFilter.add(availablePublisher.get(indexChoice));
                    } else {
                        view.print("Publisher with name %s already existed in the Publisher Filter\n", availablePublisher.get(indexChoice));
                    }
                    break;
                case EXIT:
                    return;
                default:
                    view.print("Unknown option\n");
                    break;
            }

        } while (true);
    }

    /**
     * Exit from owner view
     */
    private void ownerExit() {
        ownerLogout();
        view.print("Back to Main View\n");
    }

    /**
     * Controller for browsing book for both Customer and Owner
     * @param isCustomer
     */
    private void browseBook(boolean isCustomer) {
        ArrayList<String> availableBookNames = JDBC.getOwnedBookNames();
        ArrayList<Integer> availableISBNs = JDBC.getOwnedBookISBNs();
        ArrayList<String> availableAuthors = JDBC.getAuthors();
        ArrayList<String> availableGenre = JDBC.getGenres();
        ArrayList<String> availablePublisher = JDBC.getPublishers();

        ArrayList<String> bookNameFilter = new ArrayList<>();
        ArrayList<Integer> ISBNFilter = new ArrayList<>();
        ArrayList<String> authorFilter = new ArrayList<>();
        ArrayList<String> genreFilter = new ArrayList<>();
        ArrayList<String> publisherFilter = new ArrayList<>();

        int option;
        int indexChoice;

        final int SEARCH                    = 1;
        final int ALL_FILTERS_SHOW          = 2;
        final int ALL_FILTERS_CLEAR         = 3;
        final int BOOK_NAME_FILTER_ADD      = 4;
        final int ISBN_FILTER_ADD           = 5;
        final int AUTHOR_FILTER_ADD         = 6;
        final int GENRE_FILTER_ADD          = 7;
        final int PUBLISHER_FILTER_ADD      = 8;
        final int EXIT                      = 0;

        do {
            view.showBrowseBookMenu();
            option = view.getInt();

            switch (option) {
                case SEARCH:
                    if (isCustomer)
                        view.customerShowBooks(JDBC.customerBrowseBook(bookNameFilter, ISBNFilter, authorFilter, genreFilter, publisherFilter));
                    else
                        view.ownerShowBooks(JDBC.ownerBrowseBook(bookNameFilter, ISBNFilter, authorFilter, genreFilter, publisherFilter));
                    break;

                case ALL_FILTERS_SHOW:
                    view.print("Current Book Name Filter: %s\n", bookNameFilter.toString());
                    view.print("Current ISBN Filter: %s\n", ISBNFilter.toString());
                    view.print("Current Author Filter: %s\n", authorFilter.toString());
                    view.print("Current Genre Filter: %s\n", genreFilter.toString());
                    view.print("Current Publisher Filter: %s\n", publisherFilter.toString());
                    break;

                case ALL_FILTERS_CLEAR:
                    bookNameFilter.clear();
                    ISBNFilter.clear();
                    authorFilter.clear();
                    genreFilter.clear();
                    publisherFilter.clear();
                    view.print("All filters cleared\n");
                    break;

                case BOOK_NAME_FILTER_ADD:
                    view.print("Available Book names:\n");
                    for (int i=0; i<availableBookNames.size(); i++) {
                        view.print("(%d) %s\n", i, availableBookNames.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availableBookNames.size() - 1);
                    if (!bookNameFilter.contains(availableBookNames.get(indexChoice))) {
                        bookNameFilter.add(availableBookNames.get(indexChoice));
                    } else {
                        view.print("Book with name %s already existed in the Book Name Filter\n", availableBookNames.get(indexChoice));
                    }
                    break;

                case ISBN_FILTER_ADD:
                    view.print("Available ISBNs:\n");
                    for (int i=0; i<availableISBNs.size(); i++) {
                        view.print("(%d) ISBN %d\n", i, availableISBNs.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availableISBNs.size() - 1);
                    if (!ISBNFilter.contains(availableISBNs.get(indexChoice))) {
                        ISBNFilter.add(availableISBNs.get(indexChoice));
                    } else {
                        view.print("ISBN %d already existed in the ISBN Filter\n", availableISBNs.get(indexChoice));
                    }
                    break;

                case AUTHOR_FILTER_ADD:
                    view.print("Available Authors:\n");
                    for (int i=0; i<availableAuthors.size(); i++) {
                        view.print("(%d) %s\n", i, availableAuthors.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availableAuthors.size() - 1);
                    if (!authorFilter.contains(availableAuthors.get(indexChoice))) {
                        authorFilter.add(availableAuthors.get(indexChoice));
                    } else {
                        view.print("Author %s already existed in the Author Filter\n", availableAuthors.get(indexChoice));
                    }
                    break;

                case GENRE_FILTER_ADD:
                    view.print("Available Genres:\n");
                    for (int i=0; i<availableGenre.size(); i++) {
                        view.print("(%d) %s\n", i, availableGenre.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availableGenre.size() - 1);
                    if (!genreFilter.contains(availableGenre.get(indexChoice))) {
                        genreFilter.add(availableGenre.get(indexChoice));
                    } else {
                        view.print("Genre %s already existed in the Genre Filter\n", availableGenre.get(indexChoice));
                    }
                    break;

                case PUBLISHER_FILTER_ADD:
                    view.print("Available Publishers:\n");
                    for (int i=0; i<availablePublisher.size(); i++) {
                        view.print("(%d) %s\n", i, availablePublisher.get(i));
                    }
                    view.print("Enter the index number (in the round bracket) to add to filter\n");
                    indexChoice = view.getInt(0, availablePublisher.size() - 1);
                    if (!publisherFilter.contains(availablePublisher.get(indexChoice))) {
                        publisherFilter.add(availablePublisher.get(indexChoice));
                    } else {
                        view.print("Publisher with name %s already existed in the Publisher Filter\n", availablePublisher.get(indexChoice));
                    }
                    break;

                default:
                    break;

            }
        } while (option != EXIT);
    }

}
