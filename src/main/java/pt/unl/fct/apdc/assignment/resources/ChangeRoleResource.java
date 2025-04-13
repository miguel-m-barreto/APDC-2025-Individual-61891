package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;
import java.util.logging.Logger;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import com.google.cloud.datastore.*;

import pt.unl.fct.apdc.assignment.util.data.ChangeRoleData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreChangeRole;

@Path("/changerole")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeRoleResource {

    private static final Logger LOG = Logger.getLogger(ChangeRoleData.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data) {
        Optional<Entity> tokenOpt = DatastoreChangeRole.resolveRequesterToken(data.requesterID);

        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }

        String requesterRole = DatastoreToken.getRole(tokenOpt.get()).toUpperCase();
        return DatastoreChangeRole.processRoleChange(requesterRole, data.targetUser, data.newRole);
    }
}
