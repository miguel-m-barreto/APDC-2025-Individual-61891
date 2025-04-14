package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import com.google.cloud.datastore.*;

import pt.unl.fct.apdc.assignment.util.data.ChangeRoleData;
import pt.unl.fct.apdc.assignment.util.handler.ChangeRoleHandler;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;

@Path("/changerole")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeRoleResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data) {
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


        Entity token = tokenOpt.get();
        return ChangeRoleHandler.processChange(data, token);
    }
}
