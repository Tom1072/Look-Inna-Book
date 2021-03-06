-- Publisher
delete from Publisher;
insert into Publisher(name, email, bank_account, balance, address, phone_number)
    values ('publisher0', 'publisher0@email.com', '0000', 10000.5, '1st ABC Avenue, K1A 1X1', '613 123 4567');
insert into Publisher(name, email, bank_account, balance, address, phone_number)
    values ('publisher1', 'publisher1@email.com', '0001', 100000.5, '1st CDE Avenue, K1A 1X2', '613 123 5678');
insert into Publisher(name, email, bank_account, balance, address, phone_number)
    values ('publisher2', 'publisher2@email.com', '0002', 500.25, '1st FGH Avenue, K1A 1X3', '613 123 6789');

-- Unowned Book --> No price yet, price only set when owner collects it
delete from Book;
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages)
    values (1, 'publisher0', 'Book1', 'Genre1', 'Desciption1', 100);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages)
    values (2, 'publisher0', 'Book2', 'Genre1', 'Desciption2', 200);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages)
    values (6, 'publisher1', 'Book6', 'Genre2', 'Desciption6', 100);

-- Owner1 Books
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (3, 'publisher0', 'Book3', 'Genre1', 'Desciption3', 150, 12.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (4, 'publisher0', 'Book4', 'Genre1', 'Desciption4', 175, 15.0);
insert into Book(ISBN, publisher_name, book_name, genre, description, num_of_pages, price)
    values (5, 'publisher0', 'Book5', 'Genre2', 'Desciption5', 250, 13.0);

-- Owner2 Books
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
insert into Owner(name, bank_account, balance, email, phone_number) values ('Owner1', 'BankAccount1', 100000, 'Owner1@email.com', '613 456 7890');
insert into Owner(name, bank_account, balance, email, phone_number) values ('Owner2', 'BankAccount2', 100000, 'Owner2@email.com', '613 456 7891');

-- Collect
delete from Collect;

-- owner 1 collection
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) values (3, 'Owner1', 300, 0, 0, 1440, 0.4);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) values (4, 'Owner1', 300, 0, 0, 2250, 0.5);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) values (5, 'Owner1', 300, 0, 0, 390, 0.1);

-- owner 2 collection
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) values (7, 'Owner2', 300, 0, 0, 540, 0.1);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) values (8, 'Owner2', 300, 0, 0, 1800, 0.3);
insert into Collect(ISBN, owner_name, unit_in_stock, unit_sold, revenue, expense, publisher_split) values (9, 'Owner2', 300, 0, 0, 2400, 0.2);

-- Customer
delete from Customer;
insert into Customer(name, bank_account, balance, billing_address, shipping_address) values ('Customer1', 'BankAccount1', 100000, 'BillingAddress1', 'ShippingAddress1');
insert into Customer(name, bank_account, balance, billing_address, shipping_address) values ('Customer2', 'BankAccount2', 100000, 'BillingAddress2', 'ShippingAddress2');
insert into Customer(name, bank_account, balance, billing_address, shipping_address) values ('Customer3', 'BankAccount3', 100000, 'BillingAddress3', 'ShippingAddress3');
insert into Customer(name, bank_account, balance, billing_address, shipping_address) values ('Customer4', 'BankAccount4', 100000, 'BillingAddress3', 'ShippingAddress4');
insert into Customer(name, bank_account, balance, billing_address, shipping_address) values ('Customer5', 'BankAccount5', 100000, 'BillingAddress4', 'ShippingAddress4');
