package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;
import java.util.logging.Logger;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import com.google.gson.Gson;
import pt.unl.fct.apdc.assignment.util.data.ChangeAccountStateData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQuery;
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
        Optional<Entity> tokenOpt = DatastoreQuery.getTokenEntityByID(data.requesterID);
        Entity tokenEntity;

        if (tokenOpt.isPresent() && DatastoreToken.isTokenValid(tokenOpt.get())) {
            tokenEntity = tokenOpt.get();
        } else {
            Optional<Entity> userOpt = DatastoreQuery.getUserByUsername(data.requesterID);
            if (userOpt.isEmpty()) {
                userOpt = DatastoreQuery.getUserByEmail(data.requesterID);
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

        String requesterRole = DatastoreToken.getRole(tokenEntity).toUpperCase();
        return DatastoreChangeState.processStateChange(requesterRole, data.targetUser, data.newState);
    }
}
