package pt.unl.fct.apdc.assignment.util.validation;

import com.google.cloud.datastore.Entity;
import pt.unl.fct.apdc.assignment.util.Permission;

public class RemoveAccountValidator {

    public static boolean hasPermission(Entity requester, Entity target) {
        String requesterRole = requester.getString("session_role");
        String requesterUsername = requester.getString("session_username");

        String targetRole = target.getString("user_role");
        String targetUsername = target.getKey().getName();

        // Impedir que um user remova a sua própria conta
        if (requesterUsername.equals(targetUsername)) {
            return false;
        }

        if (targetUsername.equals("root")) {
            return false;
        }

        return Permission.canRemoveAccount(requesterRole, targetRole);
    }
}
