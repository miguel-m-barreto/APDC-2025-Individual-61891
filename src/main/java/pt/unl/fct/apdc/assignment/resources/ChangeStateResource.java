package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;
import java.util.logging.Logger;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import com.google.gson.Gson;
import pt.unl.fct.apdc.assignment.util.data.ChangeAccountStateData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreChangeState;

@Path("/changestate")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeStateResource {

    private static final Logger LOG = Logger.getLogger(ChangeStateResource.class.getName());
    private static final Gson g = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountState(ChangeAccountStateData data) {
        Optional<Entity> tokenOpt = DatastoreQueries.getToken(data.requesterID);
        
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Sessão inválida ou expirada.").build();
        }

        Entity tokenEntity = tokenOpt.get();

        String requesterRole = DatastoreToken.getRole(tokenEntity).toUpperCase();
        return DatastoreChangeState.processStateChange(requesterRole, data.targetUser, data.newState);
    }
}
