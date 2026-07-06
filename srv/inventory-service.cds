using { customer.inventory as db } from '../db/schema';

service InventoryService @(requires: 'InventoryAdmin') {
    entity Products       as projection on db.Products;
    entity Suppliers      as projection on db.Suppliers;
    entity StockMovements as projection on db.StockMovements;
}

service StockService @(requires: 'authenticated-user') {
    @readonly entity Products as projection on db.Products;
}