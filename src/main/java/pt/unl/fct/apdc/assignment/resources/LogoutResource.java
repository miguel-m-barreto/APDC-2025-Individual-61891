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


        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByTokenIDorVerifier(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou já terminada.").build();
        }

        Entity token = tokenOpt.get();

        // is token valid?
        if (!DatastoreToken.isTokenValid(token)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou já terminada.").build();
        }

        // is owner of token?
        if (!token.getString("session_username").equals(data.requesterID)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Não tens permissão para terminar esta sessão.").build();
        }

        DatastoreToken.invalidateToken(token.getKey());

        return Response.ok("{\"message\":\"Sessão terminada com sucesso.\"}").build();
    }
}
