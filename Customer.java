public class Customer {
    public String customer_id;
    public String name;
    public String billing_address;
    public String shipping_address;
    public Customer(String customer_id, String name, String billing_address, String shipping_address) {
        this.customer_id = customer_id;
        this.name = name;
        this.billing_address = billing_address ;
        this.shipping_address = shipping_address ;
    }
}
