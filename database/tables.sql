drop table OrderBook;
drop table CustomerOrder;
drop table Publish;
drop table Collect;
drop table Author;
drop table TheOrder;
drop table Book;
drop table Publisher;
drop table Owner;
drop table Customer;

create table Book (
    ISBN            integer,
    name            varchar(50),
    genre           varchar(50),
    description     varchar(50),    
    num_of_pages    integer,    
    price           real,
    primary key (ISBN)
);

create table Author(
    author_id   serial,
    ISBN        integer,
    name        varchar(50),
    primary key (author_id, ISBN),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade
);

create table Publisher(
    publisher_id    serial,
    email           varchar(50),
    bank_account    varchar(50),
    address         varchar(50),
    phone_number    bigint,
    primary key (publisher_id)
);

create table Publish(
    publisher_id    integer,
    ISBN            integer,
    primary key (publisher_id, ISBN),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade,
    foreign key (publisher_id) references Publisher(publisher_id)
        on delete cascade
);

create table Owner(
    owner_id    serial,
    name        varchar(50),
    primary key (owner_id)
);

create table Collect(
    ISBN                integer,
    owner_id            integer,
    unit_in_stock       integer,
    unit_sold           integer,
    revenue             real,
    publisher_split     real,
    primary key (ISBN),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade,
    foreign key (owner_id) references Owner(owner_id)
        on delete cascade
);

create table Customer(
    customer_id         serial,
    name                varchar(50),
    billing_address     varchar(50),
    shipping_address    varchar(50),
    primary key (customer_id)
);

create table TheOrder(
    order_id            serial,
    billing_address     varchar(50),
    shipping_address    varchar(50),
    status              int,
    ordered_date        date,
    estimated_arrival   date,
    location            varchar(50),
    primary key (order_id)
);

create table OrderBook(
    ISBN        int,
    order_id    int,
    primary key (ISBN, order_id),
    foreign key (ISBN) references Book(ISBN)
        on delete cascade,
    foreign key (order_id) references TheOrder(order_id)
        on delete cascade
);

create table CustomerOrder(
    order_id    int,
    customer_id int,
    primary key (order_id),
    foreign key (order_id) references TheOrder(order_id)
        on delete cascade,
    foreign key (customer_id) references Customer(customer_id)
        on delete cascade
);
