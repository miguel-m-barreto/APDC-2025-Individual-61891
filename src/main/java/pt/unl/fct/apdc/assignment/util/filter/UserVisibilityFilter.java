package pt.unl.fct.apdc.assignment.util.filter;

import com.google.cloud.datastore.Entity;

public class UserVisibilityFilter {

    public static boolean isVisibleTo(String role, Entity user) {
        String userRole = get(user, "user_role");
        String profile = get(user, "user_profile");
        String state = get(user, "user_account_state");

        switch (role.toUpperCase()) {
            case "ADMIN":
                return true;
            case "BACKOFFICE":
                return userRole.equalsIgnoreCase("ENDUSER");
            case "ENDUSER":
                return userRole.equalsIgnoreCase("ENDUSER") &&
                       profile.equalsIgnoreCase("public") &&
                       state.equalsIgnoreCase("ATIVADA");
            default:
                return false;
        }
    }

    public static boolean canSeeFullDetails(String role, Entity user) {
        return !role.equalsIgnoreCase("ENDUSER");
    }

    private static String get(Entity e, String field) {
        return (e.contains(field) && !e.getString(field).isBlank())
                ? e.getString(field)
                : "NOT DEFINED";
    }
}
