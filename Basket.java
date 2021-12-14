import java.util.ArrayList;

public class Basket {
    public ArrayList<BookOrder> bookOrders;

    public Basket() {
        bookOrders = new ArrayList<>();
    }

    public void add(Book book, int numOfBook) {
        boolean bookExist = false;

        for (BookOrder bookOrder:bookOrders) {
            if (bookOrder.book.equals(book)) {
                bookExist = true;
                bookOrder.unit_ordered += numOfBook;
            }
        }

        if (!bookExist && numOfBook > 0) {
            bookOrders.add(new BookOrder(book, numOfBook));
        }
    }

    public void remove(int ISBN, int numOfBook) {
        for (BookOrder bookOrder:bookOrders) {
            if (bookOrder.book.ISBN == ISBN) {
                bookOrder.unit_ordered -= numOfBook;
                if (bookOrder.unit_ordered <= 0) {
                    bookOrders.remove(bookOrder);
                }
                break;
            }
        }
    }

    public BookOrder getBookOrder(int ISBN) {
        BookOrder bookOrder = null;
        for (BookOrder bo:bookOrders) {
            if (bo.book.ISBN == ISBN) {
                bookOrder = bo;
                break;
            }
        }

        return bookOrder;
    }

    public BookOrder getBookOrderAt(int i) {
        return bookOrders.get(i);
    }

    public boolean exists(int ISBN) {
        boolean exists = false;
        for (BookOrder bo:bookOrders) {
            if (bo.book.ISBN == ISBN) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    public void clear() {
        bookOrders.clear();
    }

    /**
     * 
     * @return the total revenue on perspective of Book Owner, the total cost on perspective of Customer
     */
    public double getTotalRevenue() {
        double total = 0;
        for (BookOrder bookOrder:bookOrders) {
            total += bookOrder.getRevenue();
        }
        return total;
    }

    public String toString() {
        double total = 0;
        String s = "Basket Information:\n";
        if (bookOrders.size() > 0) {
            for (BookOrder bookOrder:bookOrders) {
                total += (bookOrder.book.price * bookOrder.unit_ordered);
                s += bookOrder;
                s += "\n";
            }
        } else {
            s += "Your basket is empty\n";
        }
        s += String.format("Your order total is: %f", total);
        return s;
    }
}
