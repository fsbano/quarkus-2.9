package io.fsbano;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

import org.jboss.logging.Logger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

@Path("/api/contact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    private static final Logger LOGGER = Logger.getLogger(ContactResource.class.getName());

    @GET
    public Uni<List<Contact>> get() {
        return Contact.listAll();
    }

    @GET
    @Path("{id}")
    public Uni<Contact> getSingle(UUID id) {
        return Contact.findById(id);
    }

    @POST
    public Uni<Response> create(Contact contact) {
        if (contact == null || contact.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return Panache.withTransaction(contact::persist)
                    .replaceWith(Response.ok(contact).status(CREATED)::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(UUID id, Contact contact) {
        if (contact == null || contact.name == null) {
            throw new WebApplicationException("Contact name was not set on request.", 422);
        }

        return Panache
                .withTransaction(() -> Contact.<Contact> findById(id)
                    .onItem().ifNotNull().invoke(entity -> entity.name = contact.name)
                    .onItem().ifNotNull().invoke(entity -> entity.email = contact.email)
                )
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(UUID id) {
        return Panache.withTransaction(() -> Contact.deleteById(id))
                .map(deleted -> deleted
                        ? Response.ok().status(NO_CONTENT).build()
                        : Response.ok().status(NOT_FOUND).build());
    }

}
