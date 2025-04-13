package pt.unl.fct.apdc.assignment.util.datastore;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.cloud.datastore.*;
import com.google.gson.JsonObject;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.unl.fct.apdc.assignment.util.Permission;

public class DatastoreChangeRole {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(DatastoreChangeRole.class.getName());

    public static Response processRoleChange(String requesterRole, String targetUser, String newRole) {
        Optional<Entity> targetOpt = DatastoreQuery.getUserByUsername(targetUser);
        if (targetOpt.isEmpty()) {
            targetOpt = DatastoreQuery.getUserByEmail(targetUser);
            if (targetOpt.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("Utilizador alvo não existe.").build();
            }
        }

        Entity target = targetOpt.get();
        String currentRole = target.getString("user_role").toUpperCase();
        newRole = newRole.toUpperCase();

        if (currentRole.equals(newRole)) {
            return Response.status(Status.BAD_REQUEST).entity("Role não alterada.\nA role atual é a mesma que a nova role.").build();
        }

        if (!isRoleValid(newRole)) {
            return Response.status(Status.BAD_REQUEST).entity("Role inválida.").build();
        }

        if (!Permission.canChangeRole(requesterRole, currentRole, newRole)) {
            return Response.status(Status.FORBIDDEN).entity("Permissão negada.").build();
        }

        Entity updatedUser = Entity.newBuilder(target)
                .set("user_role", newRole)
                .build();

        datastore.update(updatedUser);

        // get active sessions of the target user and swap the role in each session 
        // with method getActiveSessions(String username)
        List<Entity> activeSessions = DatastoreQuery.getActiveSessions(targetUser);
        for (Entity session : activeSessions) {
            Entity updatedSession = Entity.newBuilder(session)
                    .set("session_role", newRole)
                    .build();
            datastore.update(updatedSession);
        }

        JsonObject result = new JsonObject();
        result.addProperty("message", "Role de " + target.getKey().getName() + " alterado para " + newRole + " com sucesso.");
        LOG.info("Role de " + target.getKey().getName() + " alterado para " + newRole + " com sucesso.");
        return Response.ok(result.toString()).build();
    }

    public static Optional<Entity> resolveRequesterToken(String requesterID) {
        Optional<Entity> tokenOpt = DatastoreQuery.getTokenEntityByID(requesterID);

        if (tokenOpt.isPresent() && DatastoreToken.isTokenValid(tokenOpt.get())) {
            return tokenOpt;
        }

        Optional<Entity> userOpt = DatastoreQuery.getUserByUsername(requesterID);
        if (userOpt.isEmpty()) {
            userOpt = DatastoreQuery.getUserByEmail(requesterID);
            if (userOpt.isEmpty()) return Optional.empty();
        }

        String username = userOpt.get().getKey().getName();
        return DatastoreToken.getLatestValidSession(username);
    }

    private static boolean isRoleValid(String role) {
        return role.equals("ENDUSER") || role.equals("PARTNER") || role.equals("BACKOFFICE") || role.equals("ADMIN");
    }
}
