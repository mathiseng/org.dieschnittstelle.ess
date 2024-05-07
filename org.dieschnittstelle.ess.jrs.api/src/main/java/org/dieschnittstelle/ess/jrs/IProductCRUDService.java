package org.dieschnittstelle.ess.jrs;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;

import java.util.List;

/*
 * UE JRS2: 
 * deklarieren Sie hier Methoden fuer:
 * - die Erstellung eines Produkts
 * - das Auslesen aller Produkte
 * - das Auslesen eines Produkts
 * - die Aktualisierung eines Produkts
 * - das Loeschen eines Produkts
 * und machen Sie diese Methoden mittels JAX-RS Annotationen als WebService verfuegbar
 */

/*
 * TODO JRS3: aendern Sie Argument- und Rueckgabetypen der Methoden von IndividualisedProductItem auf AbstractProduct
 */
// JRS3:
@Path("/products")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface IProductCRUDService {

	// POST products
	@POST
	public IndividualisedProductItem createProduct(IndividualisedProductItem prod);

	// GET products
	@GET
	public List<IndividualisedProductItem> readAllProducts();

	// PUT
	@PUT
	@Path("/{iproductId}")
	public IndividualisedProductItem updateProduct(@PathParam("iproductId") long id,
												   IndividualisedProductItem update);

	// DELETE products/productid
	@DELETE
	@Path("/{iproductId}")
	boolean deleteProduct(@PathParam("iproductId") long id);

	// GET products/productid
	@GET
	@Path("/{iproductId}")
	public IndividualisedProductItem readProduct(@PathParam("iproductId") long id);
			
}
