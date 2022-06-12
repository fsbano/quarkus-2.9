package io.fsbano;

import static javax.ws.rs.core.Response.Status.CREATED;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

import org.jboss.logging.Logger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

@Path("/api/contact")
public class ContactResource {

    private static final Logger LOGGER = Logger.getLogger(ContactResource.class.getName());

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> create(Contact contact) {
        if (contact == null || contact.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return Panache.withTransaction(contact::persist)
                    .replaceWith(Response.ok(contact).status(CREATED)::build);
    }
}
