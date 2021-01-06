package org.dieschnittstelle.ess.mip.components.crm;

import java.util.List;

import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/touchpoints")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@WebService
public interface TouchpointAccess {

	@POST
	public AbstractTouchpoint createTouchpointAndPointOfSale(AbstractTouchpoint touchpoint) throws CrmException;

	@GET
	public List<AbstractTouchpoint> readAllTouchpoints();
	
}