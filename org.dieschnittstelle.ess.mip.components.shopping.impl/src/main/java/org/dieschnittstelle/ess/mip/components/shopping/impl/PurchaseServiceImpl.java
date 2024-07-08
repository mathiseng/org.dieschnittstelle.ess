package org.dieschnittstelle.ess.mip.components.shopping.impl;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;
import org.dieschnittstelle.ess.entities.crm.Customer;
import org.dieschnittstelle.ess.entities.crm.CustomerTransaction;
import org.dieschnittstelle.ess.entities.crm.CustomerTransactionShoppingCartItem;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.Campaign;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.shopping.ShoppingCartItem;
import org.dieschnittstelle.ess.mip.components.crm.api.CampaignTracking;
import org.dieschnittstelle.ess.mip.components.crm.api.CustomerTracking;
import org.dieschnittstelle.ess.mip.components.crm.api.TouchpointAccess;
import org.dieschnittstelle.ess.mip.components.crm.crud.api.CustomerCRUD;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystemService;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;
import org.dieschnittstelle.ess.mip.components.shopping.api.PurchaseService;
import org.dieschnittstelle.ess.mip.components.shopping.api.ShoppingException;
import org.dieschnittstelle.ess.mip.components.shopping.cart.api.ShoppingCart;
import org.dieschnittstelle.ess.mip.components.shopping.cart.api.ShoppingCartService;
import org.dieschnittstelle.ess.mip.components.shopping.cart.impl.ShoppingCartEntity;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RequestScoped
@Transactional
@Logged
public class PurchaseServiceImpl implements PurchaseService {

    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(PurchaseService.class);

    /*
     * the three beans that are used
     */
    private ShoppingCart shoppingCart;

    @Inject
    private CustomerTracking customerTracking;

    @Inject
    private CampaignTracking campaignTracking;

    /**
     * the customer
     */
    private Customer customer;

    @Inject
    private CustomerCRUD customerCRUD;

    @Inject
    private ProductCRUD productCRUD;

    @Inject
    private StockSystemService stockSystemService;

    @Inject
    private TouchpointAccess touchpointAccess;

    @Inject
    private ShoppingCartService shoppingCartService;
    /**
     * the touchpoint
     */
    private AbstractTouchpoint touchpoint;


    public void setTouchpoint(AbstractTouchpoint touchpoint) {
        this.touchpoint = touchpoint;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void addProduct(AbstractProduct product, int units) {
        this.shoppingCart.addItem(new ShoppingCartItem(product.getId(), units, product instanceof Campaign));
    }

    /*
     * verify whether campaigns are still valid
     */
    public void verifyCampaigns() throws ShoppingException {
        if (this.customer == null || this.touchpoint == null) {
            throw new RuntimeException("cannot verify campaigns! No touchpoint has been set!");
        }

        for (ShoppingCartItem item : this.shoppingCart.getItems()) {
            if (item.isCampaign()) {
                int availableCampaigns = this.campaignTracking.existsValidCampaignExecutionAtTouchpoint(
                        item.getErpProductId(), this.touchpoint);
                logger.info("got available campaigns for product " + item.getErpProductId() + ": "
                        + availableCampaigns);
                // we check whether we have sufficient campaign items available
                if (availableCampaigns < item.getUnits()) {
                    throw new ShoppingException("verifyCampaigns() failed for productBundle " + item
                            + " at touchpoint " + this.touchpoint + "! Need " + item.getUnits()
                            + " instances of campaign, but only got: " + availableCampaigns);
                }
            }
        }
    }

    public void purchase() throws ShoppingException {
        logger.info("purchase()");

        if (this.customer == null || this.touchpoint == null) {
            throw new RuntimeException(
                    "cannot commit shopping session! Either customer or touchpoint has not been set: " + this.customer
                            + "/" + this.touchpoint);
        }

        // verify the campaigns
        verifyCampaigns();

        // remove the products from stock
        checkAndRemoveProductsFromStock();

        // then we add a new customer transaction for the current purchase
        // TODO PAT1: once this functionality has been moved to the server side components, make sure
        //  that the ShoppingCartItem instances will be cloned/copied by constructing new items before
        //  using them for creating the CustomerTransaction object.
        List<ShoppingCartItem> productsInCart = this.shoppingCart.getItems();
        List<CustomerTransactionShoppingCartItem> productsInCartForTransaction = productsInCart
                .stream()
                .map(si -> new CustomerTransactionShoppingCartItem(si.getErpProductId(), si.getUnits(), si.isCampaign()))
                .collect(Collectors.toList());
        CustomerTransaction transaction = new CustomerTransaction(this.customer, this.touchpoint,
                productsInCartForTransaction);
        transaction.setCompleted(true);
        customerTracking.createTransaction(transaction);

        logger.info("purchase(): done.\n");
    }

    /*
     * TODO PAT2: complete the method implementation in your server-side component for shopping / purchasing
     */
    private void checkAndRemoveProductsFromStock() {
        logger.info("checkAndRemoveProductsFromStock");

        for (ShoppingCartItem item : this.shoppingCart.getItems()) {
            // TODO: ermitteln Sie das AbstractProduct für das gegebene ShoppingCartItem. Nutzen Sie dafür dessen erpProductId und die ProductCRUD bean
            AbstractProduct prod = productCRUD.readProduct(item.getErpProductId());
            if (item.isCampaign()) {
                this.campaignTracking.purchaseCampaignAtTouchpoint(item.getErpProductId(), this.touchpoint,
                        item.getUnits());
                // TODO: wenn Sie eine Kampagne haben, muessen Sie hier
                // 1) ueber die ProductBundle Objekte auf dem Campaign Objekt iterieren, und
                Campaign camp = (Campaign) prod;
                camp.getBundles().forEach(productBundle ->
                        removeProductFromStockIfAvailable(productBundle.getProduct(), item.getUnits() * productBundle.getUnits()));
                // 2) fuer jedes ProductBundle das betreffende Produkt in der auf dem Bundle angegebenen Anzahl, multipliziert mit dem Wert von
                // item.getUnits() aus dem Warenkorb,
                // - hinsichtlich Verfuegbarkeit ueberpruefen, und
                // - falls verfuegbar, aus dem Warenlager entfernen - nutzen Sie dafür die StockSystem bean
                // (Anm.: item.getUnits() gibt Ihnen Auskunft darüber, wie oft ein Produkt, im vorliegenden Fall eine Kampagne, im
                // Warenkorb liegt)
            } else {
                // TODO: andernfalls (wenn keine Kampagne vorliegt) muessen Sie
                // 1) das Produkt in der in item.getUnits() angegebenen Anzahl hinsichtlich Verfuegbarkeit ueberpruefen und
                // 2) das Produkt, falls verfuegbar, in der entsprechenden Anzahl aus dem Warenlager entfernen;
                removeProductFromStockIfAvailable((IndividualisedProductItem) prod, item.getUnits());
            }
        }
    }

    private void removeProductFromStockIfAvailable(IndividualisedProductItem prod, int units) {
        AtomicInteger unitsToOrder = new AtomicInteger(units);
        stockSystemService.getPointsOfSale(prod.getId()).forEach(pos -> {
            int unitsOnPos = stockSystemService.getUnitsOnStock(prod.getId(), pos);
            if (unitsToOrder.get() <= unitsOnPos) {
                stockSystemService.removeFromStock(prod.getId(), pos, unitsToOrder.get());
                return;
            } else if (unitsOnPos != 0) {
                stockSystemService.removeFromStock(prod.getId(), pos, unitsOnPos);
                unitsToOrder.addAndGet(-unitsOnPos);
            }
        });
    }

    @Override
    public void purchaseCartAtTouchpointForCustomer(long shoppingCartId, long touchpointId, long customerId) throws ShoppingException {

        this.customer = customerCRUD.readCustomer(customerId);
        this.touchpoint = touchpointAccess.readTouchpoint(touchpointId);
        this.shoppingCart = new ShoppingCartEntity();
        this.shoppingCartService.getItems(shoppingCartId).forEach(item -> {
            this.shoppingCart.addItem(new ShoppingCartItem(item.getErpProductId(), item.getUnits(), item.isCampaign()));
        });
        this.purchase();
    }
}