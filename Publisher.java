public class Publisher {
    /**
     * Store publisher information
     */
    public String name;
    public String email;
    public String bank_account;
    // public double balance;
    public String address;
    public String phone_number;

    public Publisher() {
        this.name = "";
        this.email = "";
        this.bank_account = "";
        // this.balance = -1;
        this.address = "";
        this.phone_number = "";
    }

    public String toString() {
        String s = "";
        s += String.format("Name: %s\n", this.name);
        s += String.format("Email: %s\n", this.email);
        s += String.format("Bank account: %s\n", this.bank_account);
        s += String.format("Address: %s\n", this.address);
        s += String.format("Phone number: %s\n", this.phone_number);
        return s;
    }
}
