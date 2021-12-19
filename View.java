import java.util.ArrayList;
import java.util.Scanner;

public class View {
    private Scanner scanner;

    public View() {
        scanner = new Scanner(System.in);
    }

    public void print(String format, Object... args) {
        System.out.printf(format, args);
    }

    public void showWelcomeScreen() {
        String s = "\n";
        s += "------------------------------------\n";
        s += "       Welcome to LookInnaBook      \n";
        s += "------------------------------------\n";
        s += "Menu: (type the number to choose)\n";
        s += "(1) Show Customer View\n";
        s += "(2) Show Owner View\n";
        s += "(0) Exit\n";
        s += "------------------------------------\n";
        System.out.println(s);
    }

    public void showCustomerScreenLoggedIn(Customer customer) {
        String s = "\n";
        s += "--------------------------------\n";
        s += "            Customer            \n";
        s += "--------------------------------\n";
        s += String.format("You are logged in\n");
        s += String.format("Customer information:\n");
        s += String.format("%s", customer);
        s += String.format("\n");
        s += String.format("Menu: (type the number to choose)\n");
        s += String.format("(1) Browse book that are for sale\n");
        s += String.format("(2) Add book to basket\n");
        s += String.format("(3) Remove book from basket\n");
        s += String.format("(4) Show basket\n");
        s += String.format("(5) Track orders\n");
        s += String.format("(6) Checkout\n");
        s += String.format("(7) Log out\n");
        s += String.format("(0) Exit Customer View\n");
        s += "--------------------------------\n";
        System.out.println(s);
    }

    public void showCustomerScreenNotLoggedIn() {
        String s = "\n";
        s += "--------------------------------\n";
        s += "            Customer            \n";
        s += "--------------------------------\n";
        s += String.format("You are not logged in!\n");
        s += String.format("\n");
        s += String.format("Menu: (type the number to choose)\n");
        s += String.format("(1) Browse book\n");
        s += String.format("(2) Add book to basket\n");
        s += String.format("(3) Remove book from basket\n");
        s += String.format("(4) Show basket\n");
        s += String.format("(5) Log in\n");
        s += String.format("(0) Exit Customer View\n");
        s += "--------------------------------\n";
        System.out.println(s);

    }

    public void showBrowseBookMenu() {
        String s = "";
        s += "--------------------------------\n";
        s += "           Browse Book          \n";
        s += "--------------------------------\n";
        s += "Browse book menu\n";
        s += "(1) Search using the current filter\n";
        s += "(2) Show all filters\n";
        s += "(3) Clear all filters\n";
        s += "(4) Add to Book Name filter\n";
        s += "(5) Add to ISBN filter\n";
        s += "(6) Add to Author filter\n";
        s += "(7) Add to Genre filter\n";
        s += "(8) Add to Publisher filter\n";
        s += "(0) Exit from browse book view\n";
        s += "--------------------------------\n";
        System.out.println(s);
    }

    public void customerShowBooks(ArrayList<Book> books) {
        if (books.size() == 0) {
            System.out.println("There is no book for sale.");
        } else {
            System.out.println("Here are the books for sale:");
            for (Book book:books) {
                System.out.println(book);
            }
        }
    }

    public void ownerShowBooks(ArrayList<Book> books) {
        if (books.size() == 0) {
            System.out.println("There is no book that can be added to collection.");
        } else {
            System.out.println("Here are the books that can be added to collection:");
            for (Book book:books) {
                System.out.println(book);
            }
        }
    }

    public void customerShowBasket(Basket basket) {
        System.out.println(basket);
    }

    public void showOwnerScreenLoggedIn(Owner owner) {
        String s = "\n";
        s += "--------------------------------\n";
        s += "              Owner             \n";
        s += "--------------------------------\n";
        s += String.format("You are logged in\n");
        s += String.format("Owner information:\n");
        s += String.format("%s", owner);
        s += String.format("\n");
        s += String.format("Menu: (type the number to choose)\n");
        s += String.format("(1) Browse unowned books\n");
        s += String.format("(2) Add book to collection\n");
        s += String.format("(3) Remove book from collection\n");
        s += String.format("(4) Order book from publisher\n");
        s += String.format("(5) Show all books in collection\n");
        s += String.format("(6) Show sale records\n");
        s += String.format("(9) Log out\n");
        s += String.format("(0) Exit Owner View\n");
        s += "--------------------------------\n";
        System.out.println(s);
    }

    public void showOwnerScreenNotLoggedIn() {
        String s = "\n";
        s += "--------------------------------\n";
        s += "              Owner             \n";
        s += "--------------------------------\n";
        s += String.format("You are not logged in!\n");
        s += String.format("\n");
        s += String.format("Menu: (type the number to choose)\n");
        s += String.format("(1) Browse unowned books\n");
        s += String.format("(2) Log in\n");
        s += String.format("(0) Exit Owner View\n");
        s += "--------------------------------\n";
        System.out.println(s);

    }

    public void showOwnerRecordMenu() {
        String s = "\n";
        s += "--------------------------------\n";
        s += "          Owner Record          \n";
        s += "--------------------------------\n";
        s += "Sale record menu\n";
        s += "(1) Show records using the current filters\n";
        s += "(2) Show all filters\n";
        s += "(3) Clear all filters\n";
        s += "(4) Add to Genre filter\n";
        s += "(5) Add to Author filter\n";
        s += "(6) Add to Publisher filter\n";
        s += "(0) Exit Owner Record View\n";
        s += "--------------------------------\n";
        System.out.println(s);

    }

    public void ownerBrowseFreeBook(ArrayList<Book> books) {
        if (books.size() == 0) {
            System.out.println("There is no free book left.");
        } else {
            System.out.println("Here are the books that are not owned by anyone:");
            for (Book book:books) {
                System.out.println(book);
            }
        }
    }

    public int getInt(int... bounds) {
        System.out.print("Enter your choice: ");
        int output;
        if (bounds.length == 2) {
            do {
                output = scanner.nextInt();
                if (output < bounds[0] || output > bounds[1])
                    System.out.printf("Choice not within bound (%d, %d), please enter again: ", bounds[0], bounds[1]);
                else
                    break;
            } while (true);
            scanner.nextLine();
        } else {
            output = scanner.nextInt();
            scanner.nextLine();
        }
        // System.out.println();
        return output;
    }

    public void showCollections(ArrayList<Collection> collections) {
        for (Collection collection:collections) {
            System.out.println(collection.showRecord());
        }
    }

    public Double getDouble(double... bounds) {
        System.out.print("Enter your choice: ");
        Double output;

        if (bounds.length == 2) {
            do {
                output = scanner.nextDouble();
                if (output < bounds[0] || output > bounds[1])
                    System.out.printf("Choice not within bound (%.2f, %.2f), please enter again: ", bounds[0], bounds[1]);
                else
                    break;
            } while (true);
            scanner.nextLine();
        } else {
            output = scanner.nextDouble();
            scanner.nextLine();
        }
        // System.out.println();
        return output;
    }

    public String getString() {
        System.out.print("Enter your choice: ");
        String output = scanner.nextLine();
        System.out.println();
        return output;
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
    }



}