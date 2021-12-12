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
        String s = "";
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
        String s = "";
        s += "--------------------------------\n";
        s += "            Customer            \n";
        s += "--------------------------------\n";
        s += String.format("You are logged in as %s\n", customer.name);
        s += String.format("\n");
        s += String.format("Menu: (type the number to choose)\n");
        s += String.format("(1) Browse book\n");
        s += String.format("(2) Add book to basket\n");
        s += String.format("(3) Remove book from basket\n");
        s += String.format("(5) Show basket\n");
        s += String.format("(8) Checkout\n");
        s += String.format("(9) Log out\n");
        s += String.format("(0) Exit Customer View\n");
        s += "--------------------------------\n";
        System.out.println(s);
    }

    public void showCustomerScreenNotLoggedIn() {
        String s = "";
        s += "--------------------------------\n";
        s += "            Customer            \n";
        s += "--------------------------------\n";
        s += String.format("You are not logged in!\n");
        s += String.format("\n");
        s += String.format("Menu: (type the number to choose)\n");
        s += String.format("(1) Browse book\n");
        s += String.format("(2) Add book to basket\n");
        s += String.format("(3) Remove book from basket\n");
        s += String.format("(5) Show basket\n");
        s += String.format("(9) Log in\n");
        s += String.format("(0) Exit Customer View\n");
        s += "--------------------------------\n";
        System.out.println(s);

    }

    public void customerBrowseBook(ArrayList<Book> books) {
        for (Book book:books) {
            System.out.println(book);
        }
    }

    public void customerShowBasket(Basket basket) {
        System.out.println(basket);
    }


    public void showOwnerScreen() {

    }

    public int getInt() {
        System.out.print("Enter your choice: ");
        int output = scanner.nextInt();
        scanner.nextLine();
        System.out.println();
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