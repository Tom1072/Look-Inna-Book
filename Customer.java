import java.util.ArrayList;

public class Customer {
    public String name;
    public String billing_address;
    public String shipping_address;
    public ArrayList<Order> orders;

    public Customer() {
        this.name = "";
        this.billing_address = "";
        this.shipping_address = "";
        orders = new ArrayList<>();
    }

    public String toString() {
        String s = "";
        s += String.format("name: %s\n", this.name);
        s += String.format("billing address: %s\n", this.billing_address);
        s += String.format("shipping address: %s\n", this.shipping_address);
        return s;
    }
}
