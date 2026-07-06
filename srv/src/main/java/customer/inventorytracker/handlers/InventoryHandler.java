package customer.inventorytracker.handlers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnUpdate;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;

import cds.gen.inventoryservice.InventoryService_;
import cds.gen.inventoryservice.StockMovements;
import cds.gen.inventoryservice.StockMovements_;
import cds.gen.inventoryservice.Products;
import cds.gen.inventoryservice.Products_;

@Component
@ServiceName(InventoryService_.CDS_NAME)
public class InventoryHandler implements EventHandler {

    @Autowired
    PersistenceService db;

    @Before(event = CqnService.EVENT_CREATE, entity = StockMovements_.CDS_NAME)
    public void validateAndUpdateStock(List<StockMovements> movements) {
        for (StockMovements movement : movements) {
            String productId = movement.getProductId();
            Integer quantity = movement.getQuantity();
            String type = movement.getType();

            // fetch current product
            CqnSelect sel = Select.from(Products_.class)
                .where(p -> p.ID().eq(productId));
            Products product = db.run(sel).first(Products.class)
                .orElseThrow(() -> new ServiceException(
                    ErrorStatuses.NOT_FOUND, "Product does not exist"));

            int currentStock = product.getStock();

            if ("OUT".equals(type)) {
                // validate enough stock
                if (currentStock < quantity) {
                    throw new ServiceException(
                        ErrorStatuses.BAD_REQUEST,
                        "Not enough stock. Available: " + currentStock);
                }
                product.setStock(currentStock - quantity);
            } else if ("IN".equals(type)) {
                product.setStock(currentStock + quantity);
            }

            // update product stock
            CqnUpdate update = Update.entity(Products_.class)
                .data(product).where(p -> p.ID().eq(productId));
            db.run(update);
        }
    }
}