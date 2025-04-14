package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.data.ChangeAttributesData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.handler.ChangeAttributesHandler;

import java.util.Optional;

@Path("/changeattributes")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeAttributesResource {

    @POST
    @Path("/own")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateOwnAttributes(ChangeAttributesData data) {
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

        

        Entity tokenEntity = tokenOpt.get();
        return ChangeAttributesHandler.applyChangesForEndUser(data, tokenEntity);
    }

    @POST
    @Path("/backoffice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response backofficeUpdate(ChangeAttributesData data) {
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

        
        Entity tokenEntity = tokenOpt.get();
        return ChangeAttributesHandler.applyChangesForBackoffice(data, tokenEntity);
    }

    @POST
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adminUpdate(ChangeAttributesData data) {
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


        Entity tokenEntity = tokenOpt.get();
        return ChangeAttributesHandler.applyChangesForAdmin(data, tokenEntity);
    }
}
