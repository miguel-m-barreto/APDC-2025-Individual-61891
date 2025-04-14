package pt.unl.fct.apdc.assignment.util.validation;

import pt.unl.fct.apdc.assignment.util.Permission;
import pt.unl.fct.apdc.assignment.util.data.ChangeAttributesData;

import com.google.cloud.datastore.Entity;

public class ChangeAttributesValidator {

    public static boolean hasPermission(Entity requester, Entity target, ChangeAttributesData data) {
        String requesterRole = requester.getString("session_role");
        String requesterUsername = requester.getString("session_username");

        String targetUsername = target.getKey().getName();
        String targetRole = target.getString("user_role");
        String targetState = target.getString("user_account_state");

        boolean targetIsActivated = "ATIVADA".equalsIgnoreCase(targetState);
        boolean changingControlFields = data.role != null || data.state != null;
        boolean changingUserIdFields = data.username != null || data.email != null;

        return Permission.canChangeAttributes(
                requesterRole, requesterUsername, targetUsername,
                targetRole, targetIsActivated,
                changingControlFields, changingUserIdFields
        );
    }
}
