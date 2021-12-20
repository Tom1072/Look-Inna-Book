import java.util.ArrayList;

public class Customer {
    /**
     * Store Customer information when logged in
     */
    public String name;
    public String billing_address;
    public String shipping_address;
    public Double balance;
    public ArrayList<Order> orders;

    public Customer() {
        this.name = "";
        this.billing_address = "";
        this.shipping_address = "";
        orders = new ArrayList<>();
    }

    public String toString() {
        String s = "";
        s += String.format("\tname: %s\n", this.name);
        s += String.format("\tbilling address: %s\n", this.billing_address);
        s += String.format("\tshipping address: %s\n", this.shipping_address);
        s += String.format("\tbalance: $%.2f\n", this.balance);
        return s;
    }
}
