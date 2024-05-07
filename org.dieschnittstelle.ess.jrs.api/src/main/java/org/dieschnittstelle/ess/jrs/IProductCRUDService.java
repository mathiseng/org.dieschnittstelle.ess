package org.dieschnittstelle.ess.jrs;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;

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
@Path("/products") // base URL die jeder Request hat
@Consumes({MediaType.APPLICATION_JSON}) //Ich erwarte Daten im JSON Format
@Produces({MediaType.APPLICATION_JSON}) //Ich übergebe Daten im JSON Format
public interface IProductCRUDService {

	// POST products
	@POST
    AbstractProduct createProduct(AbstractProduct prod);

	//GET /products/
	@GET
    List<AbstractProduct> readAllProducts();

	//PUT /products/<id>
	@PUT
	@Path("/{iproductId}")
    AbstractProduct updateProduct(@PathParam("iproductId") long id,
                                  AbstractProduct update);

	//DELETE /products/<id>	@DELETE
	@Path("/{iproductId}")
	boolean deleteProduct(@PathParam("iproductId") long id);

	//GET /products/<id>
	@GET
	@Path("/{iproductId}")
    AbstractProduct readProduct(@PathParam("iproductId") long id);
			
}
