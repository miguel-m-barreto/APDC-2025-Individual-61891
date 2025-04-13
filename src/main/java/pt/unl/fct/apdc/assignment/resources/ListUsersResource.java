package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Entity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import pt.unl.fct.apdc.assignment.util.data.ListUsersData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreListUsers;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;

import java.util.Optional;


@Path("/listusers")
@Produces(MediaType.APPLICATION_JSON)
public class ListUsersResource {

    private static final Gson g = new Gson();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listUsers(ListUsersData data) {
        Optional<Entity> tokenOpt = DatastoreQueries.getToken(data.identifier);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }

        Entity tokenEntity = tokenOpt.get();

        String requesterRole = DatastoreToken.getRole(tokenEntity).toUpperCase();
        JsonArray result = DatastoreListUsers.buildVisibleUsers(requesterRole);

        JsonObject response = new JsonObject();
        response.addProperty("Existing users: ", result.size());
        response.add("users", result);
        return Response.ok(g.toJson(response)).build();
    }

}
