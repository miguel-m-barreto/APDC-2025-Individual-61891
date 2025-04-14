package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.apdc.assignment.util.data.RemoveAccountData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.handler.RemoveAccountHandler;

import java.util.Optional;

@Path("/removeaccount")
@Produces(MediaType.APPLICATION_JSON)
public class RemoveAccountResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAccount(RemoveAccountData data) {
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

        return RemoveAccountHandler.removeAccount(data, tokenOpt.get());
    }
}
