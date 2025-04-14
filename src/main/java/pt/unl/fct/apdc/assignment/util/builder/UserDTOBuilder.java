package pt.unl.fct.apdc.assignment.util.builder;

import com.google.cloud.datastore.Entity;
import com.google.gson.JsonObject;

public class UserDTOBuilder {

    public static JsonObject toJson(Entity user, boolean full) {
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
            obj.addProperty("employer_nif", get(user, "user_employer_nif"));
            obj.addProperty("creation_time", get(user, "user_creation_time"));
            obj.addProperty("photo", get(user, "user_photo_url"));
        }

        return obj;
    }

    private static String get(Entity e, String field) {
        if (!e.contains(field)) return "NOT DEFINED";

        try {
            String val = e.getString(field);
            return (val == null || val.trim().isEmpty()) ? "NOT DEFINED" : val;
        } catch (Exception ex) {
            return "NOT DEFINED";
        }
    }
}
