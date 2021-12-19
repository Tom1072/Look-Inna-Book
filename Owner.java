public class Owner {
    public String name;
    public String bank_account;
    public String email;
    public String phone_number;
    public Double balance;

    public String toString() {
        String s = "";
        s += String.format("\tName: %s\n", name);
        s += String.format("\tBank account: %s\n", bank_account);
        s += String.format("\tEmail: %s\n", email);
        s += String.format("\tPhone number: %s\n", phone_number);
        s += String.format("\tBalance: $%.2f\n", balance);
        return s;

    }

}
