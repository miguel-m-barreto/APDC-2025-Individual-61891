package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import pt.unl.fct.apdc.assignment.util.data.ListUsersData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.handler.ListUsersHandler;

import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;

import java.util.Optional;


@Path("/listusers")
@Produces(MediaType.APPLICATION_JSON)
public class ListUsersResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listUsers(ListUsersData data) {
        /// Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();

        String requesterRole = DatastoreToken.getRole(tokenOpt.get()).toUpperCase();
        return ListUsersHandler.handle(requesterRole);
    }
}


