package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystemService;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.PointOfSaleCRUD;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.List;

import static org.dieschnittstelle.ess.utils.Utils.show;

@ApplicationScoped
@Transactional
@Logged
@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
public class StockSystemServiceImpl implements StockSystemService {

    @Inject
    private StockSystem stockSystem;

    @Inject
    private ProductCRUD productCRUD;

    @Override
    public void addToStock(long productId, long pointOfSaleId, int units) {
        show("stockSystem is: %s of type %s ", stockSystem, stockSystem.getClass());
        show("productCrud is: %s of type %s ", productCRUD, productCRUD.getClass());

        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);
        stockSystem.addToStock(prod, pointOfSaleId, units);
    }

    @Override
    public void removeFromStock(long productId, long pointOfSaleId, int units) {
        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);
        stockSystem.removeFromStock(prod, pointOfSaleId, units);
    }

    @Override
    public List<IndividualisedProductItem> getProductsOnStock(long pointOfSaleId) {
        return isIdValid(pointOfSaleId) ? stockSystem.getProductsOnStock(pointOfSaleId) : stockSystem.getAllProductsOnStock();
    }

    @Override
    public int getUnitsOnStock(long productId, long pointOfSaleId) {
        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);

        return isIdValid(pointOfSaleId) ? stockSystem.getUnitsOnStock(prod, pointOfSaleId) : stockSystem.getTotalUnitsOnStock(prod);
    }

    @Override
    public List<Long> getPointsOfSale(long productId) {
        IndividualisedProductItem prod = (IndividualisedProductItem) productCRUD.readProduct(productId);
        return stockSystem.getPointsOfSale(prod);
    }

    private boolean isIdValid(long id) {
        return id > 0;
    }
}
