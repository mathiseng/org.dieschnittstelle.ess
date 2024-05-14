package org.dieschnittstelle.ess.jrs;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import org.dieschnittstelle.ess.entities.GenericCRUDExecutor;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;

import java.util.List;

/*
 * TODO JRS2: implementieren Sie hier die im Interface deklarierten Methoden
 */

public class ProductCRUDServiceImpl implements IProductCRUDService {

    //ServiceContext einfügen
    private GenericCRUDExecutor<AbstractProduct> productCRUD;


    public ProductCRUDServiceImpl(@Context ServletContext servletContext, @Context HttpServletRequest request) {
        this.productCRUD = (GenericCRUDExecutor<AbstractProduct>) servletContext.getAttribute("productCRUD");
    }

    @Override
    public AbstractProduct createProduct(
            AbstractProduct prod) {
        return productCRUD.createObject(prod);
    }

    @Override
    public List<AbstractProduct> readAllProducts() {
        return (List) productCRUD.readAllObjects();
    }

    @Override
    public AbstractProduct updateProduct(long id,
                                         AbstractProduct update) {
        update.setId(id);
        return productCRUD.updateObject(update);
    }

    @Override
    public boolean deleteProduct(long id) {
        return productCRUD.deleteObject(id);
    }

    @Override
    public AbstractProduct readProduct(long id) {
        AbstractProduct product = productCRUD.readObject(id);
        if (product != null) {
            return product;
        } else {
            throw new NotFoundException("The touchpoint with id " + id + " does not exist!");
        }
    }

}
