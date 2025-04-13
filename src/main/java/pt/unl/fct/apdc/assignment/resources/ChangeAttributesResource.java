package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.data.ChangeAttributesData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreChangeAttributes;

import java.util.Optional;

@Path("/changeattributes")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeAttributesResource {

    private static final Gson g = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAttributes(ChangeAttributesData data) {
        // Validar dados de atributos
        if (!data.validAttributes()) {
            return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Dados inválidos para alteração de atributos.").build();
        }

        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByVerifier(data.requesterID);
        Entity tokenEntity;

        if (tokenOpt.isPresent() && DatastoreToken.isTokenValid(tokenOpt.get())) {
            tokenEntity = tokenOpt.get();
        } else {
            tokenOpt = DatastoreQueries.getTokenEntityByID(data.requesterID);

            if (tokenOpt.isPresent() && DatastoreToken.isTokenValid(tokenOpt.get())) {
                tokenEntity = tokenOpt.get();
            } else {
                Optional<Entity> userOpt = DatastoreQueries.getUserByUsername(data.requesterID);
                if (userOpt.isEmpty()) {
                    userOpt = DatastoreQueries.getUserByEmail(data.requesterID);
                    if (userOpt.isEmpty()) {
                        return Response.status(Response.Status.UNAUTHORIZED).entity("Requester não encontrado.").build();
                    }
                }
                String username = userOpt.get().getKey().getName();
                tokenOpt = DatastoreToken.getLatestValidSession(username);
                if (tokenOpt.isEmpty()) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
                }
                tokenEntity = tokenOpt.get();
            }
        }

        String requesterUsername = DatastoreToken.getUsername(tokenEntity);
        String requesterRole = DatastoreToken.getRole(tokenEntity);

        return DatastoreChangeAttributes.processAttributeUpdate(data, requesterUsername, requesterRole);
    }
}
