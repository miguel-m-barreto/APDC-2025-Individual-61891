package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DatastoreListUsers {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final String ENDUSER_ROLE = "ENDUSER";

    public static JsonArray buildVisibleUsers(String requesterRole) {
        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
        QueryResults<Entity> results = datastore.run(query);

        JsonArray users = new JsonArray();

        while (results.hasNext()) {
            Entity user = results.next();
            String role = get(user, "user_role");
            String profile = get(user, "user_profile");
            String state = get(user, "user_account_state");

            switch (requesterRole) {
                case "ADMIN":
                    users.add(toJson(user, true));
                    break;
                case "BACKOFFICE":
                    if (role.equals(ENDUSER_ROLE)) users.add(toJson(user, true));
                    break;
                case "ENDUSER":
                    if (role.equals(ENDUSER_ROLE) && profile.equalsIgnoreCase("public") && state.equalsIgnoreCase("ATIVADA")) {
                        users.add(toJson(user, false));
                    }
                    break;
            }
        }

        return users;
    }

    private static JsonObject toJson(Entity user, boolean full) {
        JsonObject obj = new JsonObject();
        obj.addProperty("username", user.getKey().getName());
        obj.addProperty("email", get(user, "user_email"));
        obj.addProperty("name", get(user, "user_name"));

        if (full) {
            obj.addProperty("role", get(user, "user_role"));
            obj.addProperty("phone", get(user, "user_phone"));
            obj.addProperty("profile", get(user, "user_profile"));
            obj.addProperty("state", get(user, "user_account_state"));
            obj.addProperty("nif", get(user, "user_nif"));
            obj.addProperty("cc", get(user, "user_cc"));
            obj.addProperty("employer", get(user, "user_employer"));
            obj.addProperty("job", get(user, "user_job"));
            obj.addProperty("address", get(user, "user_address"));
            obj.addProperty("employer nif", get(user, "user_employer_nif"));
            obj.addProperty("creation time", get(user, "user_creation_time"));
            obj.addProperty("photo", get(user, "user_photo_url"));
        }

        return obj;
    }

    private static String get(Entity e, String field) {
        if (!e.contains(field)) {
            return "NOT DEFINED";
        }
    
        try {
            String val = e.getString(field);
            if (val == null || val.trim().isEmpty() || val.trim().equals("NOT DEFINED")) {
                return "NOT DEFINED";
            }
            return val;
        } catch (Exception ex) {
            return "NOT DEFINED";
        }
    }
}
