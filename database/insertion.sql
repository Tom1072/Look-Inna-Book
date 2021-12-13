-- Publisher
delete from Publisher;
insert into Publisher(name, email, bank_account, balance, address, phone_number)
    values ('publisher0', 'publisher0@email.com', '0000', 10000.5, '1st ABC Avenue, K1A 1X1', '613 123 4567');
insert into Publisher(name, email, bank_account, balance, address, phone_number)
    values ('publisher1', 'publisher1@email.com', '0001', 100000.5, '1st CDE Avenue, K1A 1X2', '613 123 5678');
insert into Publisher(name, email, bank_account, balance, address, phone_number)
    values ('publisher2', 'publisher2@email.com', '0002', 500.25, '1st FGH Avenue, K1A 1X3', '613 123 6789');

-- Book
delete from Book;
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (1, 'publisher0', 'Book1', 'Genre1', 'Desciption1', 100, 10.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (2, 'publisher0', 'Book2', 'Genre1', 'Desciption2', 200, 11.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (3, 'publisher0', 'Book3', 'Genre1', 'Desciption3', 150, 12.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (4, 'publisher0', 'Book4', 'Genre1', 'Desciption4', 175, 15.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (5, 'publisher0', 'Book5', 'Genre2', 'Desciption5', 250, 13.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (6, 'publisher1', 'Book6', 'Genre2', 'Desciption6', 100, 11.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (7, 'publisher1', 'Book7', 'Genre3', 'Desciption7', 600, 18.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (8, 'publisher1', 'Book8', 'Genre4', 'Desciption8', 700, 20.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (9, 'publisher2', 'Book9', 'Genre4', 'Desciption9', 700, 40.0);

-- Author
delete from Author;

-- books with separate authors
insert into Author(ISBN, name) values (1, 'Author1');
insert into Author(ISBN, name) values (2, 'Author2');
insert into Author(ISBN, name) values (3, 'Author3');
insert into Author(ISBN, name) values (4, 'Author4');
insert into Author(ISBN, name) values (5, 'Author5');
insert into Author(ISBN, name) values (6, 'Author6');

-- 2 books writen by the same author
insert into Author(ISBN, name) values ( 7, 'Author7');
insert into Author(ISBN, name) values ( 8, 'Author7');

-- book cowriten by 3 different authors
insert into Author(ISBN, name) values (9, 'Author8');
insert into Author(ISBN, name) values (9, 'Author9');
insert into Author(ISBN, name) values (9, 'Author10');

-- Owner
delete from Owner;
insert into Owner(name, bank_account, email, phone_number) values ('Owner1', 'BankAccount1', 'Owner1@email.com', '613 456 7890');
insert into Owner(name, bank_account, email, phone_number) values ('Owner2', 'BankAccount2', 'Owner2@email.com', '613 456 7891');

-- Collect
delete from Collect;

-- owner 1 collection
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (1, 'Owner1', 0, 0, 0, 0.2);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (2, 'Owner1', 0, 0, 0, 0.3);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (3, 'Owner1', 0, 0, 0, 0.4);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (4, 'Owner1', 0, 0, 0, 0.5);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (5, 'Owner1', 0, 0, 0, 0.1);

-- owner 2 collection
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (6, 'Owner2', 0, 0, 0, 0.2);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (7, 'Owner2', 0, 0, 0, 0.1);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (8, 'Owner2', 0, 0, 0, 0.3);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, publisher_split) values (9, 'Owner2', 0, 0, 0, 0.2);

-- Customer
delete from Customer;
insert into Customer(name, billing_address, shipping_address) values ('Customer1', 'BillingAddress1', 'ShippingAddress1');
insert into Customer(name, billing_address, shipping_address) values ('Customer2', 'BillingAddress2', 'ShippingAddress2');
insert into Customer(name, billing_address, shipping_address) values ('Customer3', 'BillingAddress3', 'ShippingAddress3');
insert into Customer(name, billing_address, shipping_address) values ('Customer4', 'BillingAddress3', 'ShippingAddress4');
insert into Customer(name, billing_address, shipping_address) values ('Customer5', 'BillingAddress4', 'ShippingAddress4');

-- Order, CustomerOrder, and OrderBook
-- delete from TheOrder;

-- insert into TheOrder (billing_address, shipping_address, status, ordered_date, estimated_arrival, location)
--     values ('BillingAddress1', 'DifferentShippingAddress1', 'Shipping', '2021-01-01', '2021-01-10', 'Location1');

-- insert into TheOrder (billing_address, shipping_address, status, ordered_date, estimated_arrival, location)
--     values ('BillingAddress1', 'ShippingAddress2', 'Shipping', '2021-02-02', '2021-02-20', 'Location2');

-- delete from CustomerOrder;

-- insert into CustomerOrder(order_id, customer_id) values (1, 1);
-- insert into CustomerOrder(order_id, customer_id) values (2, 2);

-- delete from OrderBook

-- insert into OrderBook(ISBN, order_id, unit_ordered) values (1, 1, 10);
-- insert into OrderBook(ISBN, order_id, unit_ordered) values (2, 1, 1);
-- insert into OrderBook(ISBN, order_id, unit_ordered) values (3, 1, 2);
-- insert into OrderBook(ISBN, order_id, unit_ordered) values (4, 1, 4);

-- insert into OrderBook(ISBN, order_id, unit_ordered) values (4, 2, 1);
-- insert into OrderBook(ISBN, order_id, unit_ordered) values (3, 2, 1);

-- update Collect
-- set unit_in_stock=unit_in_stock-10,
-- where ISBN=1;

-- update Collect
-- set unit_in_stock=unit_in_stock-1,
-- where ISBN=1;

-- Steps when placing an order
-- Add a new entry to Order table
-- insert into TheOrder(billing_address, shipping_address, status, ordered_date, estimated_arrival, location)
--     values ('Billing1', 'Shipping1', 'Order Placed', '2021-12-13', '2021-12-15', 'Warehouse');

-- Link the order with the customer
-- insert into CustomerOrder(order_id, customer_name)
--     values (1, 'Customer1');

-- Link the books with the order
-- insert into OrderBook(ISBN, order_id, unit_ordered)
--     values (1, 1, 1);