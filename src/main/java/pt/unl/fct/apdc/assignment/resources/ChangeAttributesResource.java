package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAttributes(ChangeAttributesData data) {
        // Validar dados de atributos
        if (!data.validAttributes()) {
            return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Dados inválidos para alteração de atributos.").build();
        }

        Optional<Entity> tokenOpt = DatastoreQueries.getToken(data.requesterID);
        
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Sessão inválida ou expirada.").build();
        }

        Entity tokenEntity = tokenOpt.get();
        String requesterUsername = DatastoreToken.getUsername(tokenEntity);
        String requesterRole = DatastoreToken.getRole(tokenEntity);

        return DatastoreChangeAttributes.processAttributeUpdate(data, requesterUsername, requesterRole);
    }
}
