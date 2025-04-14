package pt.unl.fct.apdc.assignment.util.handler;

import com.google.cloud.datastore.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.builder.UserDTOBuilder;
import pt.unl.fct.apdc.assignment.util.filter.UserVisibilityFilter;

public class ListUsersHandler {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Response handle(String requesterRole) {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
        QueryResults<Entity> results = datastore.run(query);

        JsonArray users = new JsonArray();

        while (results.hasNext()) {
            Entity user = results.next();

            if (UserVisibilityFilter.isVisibleTo(requesterRole, user)) {
                boolean full = UserVisibilityFilter.canSeeFullDetails(requesterRole, user);
                users.add(UserDTOBuilder.toJson(user, full));
            }
        }

        JsonObject response = new JsonObject();
        response.addProperty("Existing users: ", users.size());
        response.add("users", users);

        return Response.ok(response.toString()).build();
    }
}
