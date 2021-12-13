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

    public String toString() {
        String s = "Basket Information:\n";
        if (bookOrders.size() > 0) {
            for (BookOrder bookOrder:bookOrders) {
                s += bookOrder;
                s += "\n";
            }
        } else {
            s += "Your basket is empty\n";
        }
        return s;
    }
}
