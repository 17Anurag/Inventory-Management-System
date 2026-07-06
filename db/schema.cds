namespace customer.inventory;

using { cuid, managed } from '@sap/cds/common';

entity Products : cuid, managed {
    name        : String(100) not null;
    description : String(500);
    category    : String(50);
    price       : Decimal(10, 2);
    stock       : Integer default 0;
    unit        : String(10) default 'EA';
    supplier    : Association to Suppliers;
}

entity Suppliers : cuid, managed {
    name         : String(100) not null;
    contactEmail : String(100);
    country      : String(50);
    products     : Association to many Products 
                   on products.supplier = $self;
}

entity StockMovements : cuid, managed {
    product  : Association to Products not null;
    type     : String(3) not null;
    quantity : Integer not null;
    remarks  : String(200);
}