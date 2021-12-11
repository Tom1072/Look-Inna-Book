public class Order {
    public int order_id;
    public String billing_address;
    public String shipping_address;
    public String status;
    public String ordered_date;
    public String estimated_arrival;
    public String location;
    
    public Order(int order_id, String billing_address, String shipping_address, String status, String ordered_date, String estimated_arrival, String location) {
        this.order_id = order_id;
        this.billing_address = billing_address;
        this.shipping_address = shipping_address;
        this.status = status;
        this.ordered_date = ordered_date;
        this.estimated_arrival = estimated_arrival;
        this.location = location;
    }
}


