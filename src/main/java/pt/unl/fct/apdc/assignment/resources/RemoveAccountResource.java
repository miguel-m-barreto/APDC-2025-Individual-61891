package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;
import java.util.logging.Logger;

import com.google.cloud.datastore.Entity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.apdc.assignment.util.datastore.*;
import pt.unl.fct.apdc.assignment.util.data.RemoveAccountData;

@Path("/removeaccount")
@Produces(MediaType.APPLICATION_JSON)
public class RemoveAccountResource {

    private static final Logger LOG = Logger.getLogger(RemoveAccountData.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAccount(RemoveAccountData data) {
        Optional<Entity> tokenOpt = DatastoreQueries.getToken(data.requesterID);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Sessão inválida ou expirada.").build();
        }

        Entity tokenEntity = tokenOpt.get();

        String requesterRole = DatastoreToken.getRole(tokenEntity).toUpperCase();
        LOG.info("Requester role: " + requesterRole + " | Target user: " + data.targetUser + " | Remove account");
        return DatastoreRemoveAccount.processAccountRemoval(requesterRole, data.targetUser);
    }
}
