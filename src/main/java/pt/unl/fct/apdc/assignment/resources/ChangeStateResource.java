package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;
import java.util.logging.Logger;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.apdc.assignment.util.data.ChangeAccountStateData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreChangeState;

@Path("/changestate")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeStateResource {

    private static final Logger LOG = Logger.getLogger(ChangeStateResource.class.getName());

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
        LOG.info("Requester role: " + requesterRole + " | Target user: " + data.targetUser + " | New state: " + data.newState);
        return DatastoreChangeState.processStateChange(requesterRole, data.targetUser, data.newState);
    }
}
