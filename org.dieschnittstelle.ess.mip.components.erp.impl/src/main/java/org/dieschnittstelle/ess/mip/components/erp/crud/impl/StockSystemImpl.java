package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.erp.PointOfSale;
import org.dieschnittstelle.ess.entities.erp.StockItem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystem;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.PointOfSaleCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Transactional
@Logged
public class StockSystemImpl implements StockSystem {

    @Inject
    private StockItemCRUD stockItemCRUD;

    @Inject
    private PointOfSaleCRUD posCRUD;

    @Override
    public void addToStock(IndividualisedProductItem product, long pointOfSaleId, int units) {
        PointOfSale pos = posCRUD.readPointOfSale(pointOfSaleId);
        StockItem stockItem = stockItemCRUD.readStockItem(product, pos);

        if (stockItem != null) {
            stockItem.setUnits(stockItem.getUnits() + units);
            //FRAGE: Warum müssen wir die updateCRUD nicht aufrufen und es funktioniert trotzdem?
            stockItemCRUD.updateStockItem(stockItem);
        } else {
            if (units > 0) {
                stockItem = new StockItem(product, pos, units);
                stockItemCRUD.createStockItem(stockItem);
            }
        }
    }

    @Override
    public void removeFromStock(IndividualisedProductItem product, long pointOfSaleId, int units) {
        addToStock(product, pointOfSaleId, -units);
    }

    @Override
    public List<IndividualisedProductItem> getProductsOnStock(long pointOfSaleId) {
        PointOfSale pos = posCRUD.readPointOfSale(pointOfSaleId);
        List<StockItem> stockItemList = stockItemCRUD.readStockItemsForPointOfSale(pos);
        List<IndividualisedProductItem> products = new ArrayList<>();
        for (StockItem stockItem1 : stockItemList) {
            products.add(stockItem1.getProduct());
        }
        return products;
    }

    @Override
    public List<IndividualisedProductItem> getAllProductsOnStock() {
        List<PointOfSale> posList = posCRUD.readAllPointsOfSale();
        Set<IndividualisedProductItem> setOfProducts = new HashSet<IndividualisedProductItem>();
        posList.forEach(pos -> {
            setOfProducts.addAll(getProductsOnStock(pos.getId()));
        });
        return new ArrayList<>(setOfProducts);
    }

    @Override
    public int getUnitsOnStock(IndividualisedProductItem product, long pointOfSaleId) {
        PointOfSale pos = posCRUD.readPointOfSale(pointOfSaleId);
        StockItem stockItem = stockItemCRUD.readStockItem(product, pos);
        return stockItem != null ? stockItem.getUnits() : 0;
    }

    @Override
    public int getTotalUnitsOnStock(IndividualisedProductItem product) {
        List<StockItem> stockItemList = stockItemCRUD.readStockItemsForProduct(product);
        AtomicInteger totalUnits = new AtomicInteger();
        stockItemList.forEach(item -> {
            totalUnits.addAndGet(item.getUnits());
        });

        return totalUnits.intValue();
    }

    @Override
    public List<Long> getPointsOfSale(IndividualisedProductItem product) {
        List<StockItem> stockItemList = stockItemCRUD.readStockItemsForProduct(product);
        List<Long> pointsOfSaleIds = new ArrayList<>();
        stockItemList.forEach(item -> pointsOfSaleIds.add(item.getPos().getId()));

        return pointsOfSaleIds;
    }
}
