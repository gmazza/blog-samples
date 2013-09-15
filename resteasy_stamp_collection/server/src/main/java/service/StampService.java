package service;

import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

/**
* This interface describes a JAX-RS root resource. All the JAXRS annotations (except those overridden) will
* be inherited by classes implementing it.
*/
@Path("/stamps")
public interface StampService {

   /**
    * Sub-resource locator (note the absence of HTTP Verb annotations such as GET). It locates a Stamp
    * instance with a provided id and delegates to it to process the request.
    */
    @Path("/{id}")
    public StampResource getStampResource(@PathParam("id") String id);

    /**
     * Returns an explicit collection of all stamps in XML format in response to HTTP GET requests
     */
    @GET
    @Produces("application/xml")
    public Collection<StampResource> getAllStamps();
}
