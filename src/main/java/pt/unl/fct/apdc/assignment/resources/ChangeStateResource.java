package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.apdc.assignment.util.data.ChangeAccountStateData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.handler.ChangeStateHandler;

@Path("/changestate")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeStateResource {

    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAsAdmin(ChangeAccountStateData data) {
        if (!data.validAttributes()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();
        }

        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();


        Entity requester = tokenOpt.get();
        return ChangeStateHandler.changeStateForAdmin(data, requester);
    }

    @POST
    @Path("/backoffice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAsBackoffice(ChangeAccountStateData data) {
        if (data == null || data.requesterID == null || data.targetUser == null || data.newState == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados incompletos.").build();

        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();

        Entity requester = tokenOpt.get();
        return ChangeStateHandler.changeStateForBackoffice(data, requester);
    }
}
