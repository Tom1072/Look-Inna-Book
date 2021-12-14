drop table OrderBook;
drop table CustomerOrder;
drop table Collect;
drop table Author;
drop table TheOrder;
drop table Book;
drop table Publisher;
drop table Owner;
drop table Customer;

create table Publisher(
    name           varchar(50),
    email           varchar(50),
    bank_account    varchar(50),
    balance         real,
    address         varchar(50),
    phone_number    varchar(20),
    primary key (name)
);

create table Book (
    ISBN            integer,
    publisher_name  varchar(50),
    book_name       varchar(50),
    genre           varchar(50),
    description     varchar(50),    
    num_of_pages    integer,    
    price           real,
    primary key (ISBN),
    foreign key (publisher_name) references Publisher(name)
);

create table Author(
    ISBN        integer,
    name        varchar(50),
    primary key (ISBN, name),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade
);

create table Owner(
    name            varchar(50),
    bank_account    varchar(50),
    email           varchar(50),
    phone_number    varchar(50),
    primary key (name)
);

create table Collect(
    ISBN                integer,
    owner_name          varchar(50),
    unit_in_stock       integer check (unit_in_stock >= 0),
    unit_sold           integer,
    revenue             real,
    publisher_split     real,
    primary key (ISBN),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade,
    foreign key (owner_name) references Owner(name)
        on delete cascade
);

create table Customer(
    name                varchar(50),
    billing_address     varchar(50),
    shipping_address    varchar(50),
    primary key (name)
);

-- PSQL doesn't accept "Order" as the table name :)
create table TheOrder(
    order_id            serial,
    billing_address     varchar(50),
    shipping_address    varchar(50),
    status              varchar(50),
    ordered_date        date,
    estimated_arrival   date,
    location            varchar(50),
    primary key (order_id)
);

create table OrderBook(
    ISBN            int,
    order_id        int,
    unit_ordered    int,
    primary key (ISBN, order_id),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade,
    foreign key (order_id) references TheOrder(order_id)
        on delete cascade
);

create table CustomerOrder(
    order_id        int,
    customer_name   varchar(50),
    primary key (order_id),
    foreign key (order_id) references TheOrder(order_id)
        on delete cascade,
    foreign key (customer_name) references Customer(name)
        on delete cascade
);

-- Triggers