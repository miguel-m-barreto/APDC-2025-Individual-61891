package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.apdc.assignment.util.data.LogoutData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;

import java.util.Optional;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON)
public class LogoutResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(LogoutData data) {
        if (!data.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Identificador inválido.").build();
        }


        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();

        DatastoreToken.invalidateToken(tokenOpt.get().getKey());

        return Response.ok("{\"message\":\"Sessão terminada com sucesso.\"}").build();
    }
}
