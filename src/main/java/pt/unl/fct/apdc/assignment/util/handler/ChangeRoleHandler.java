package pt.unl.fct.apdc.assignment.util.handler;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.Permission;
import pt.unl.fct.apdc.assignment.util.data.ChangeRoleData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ChangeRoleHandler {

    private static final Datastore ds = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(ChangeRoleHandler.class.getName());

    public static Response processChange(ChangeRoleData data, Entity requesterToken) {
        String requesterRole = requesterToken.getString("session_role").toUpperCase();

        Optional<Entity> targetOpt = DatastoreQueries.getUserByUsernameOrEmail(data.targetUser);
        if (targetOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Utilizador alvo não encontrado.").build();
        }

        Entity target = targetOpt.get();
        String currentRole = target.getString("user_role").toUpperCase();
        String newRole = data.newRole.toUpperCase();
        String targetUsername = target.getKey().getName();

        if (currentRole.equals(newRole)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A role atual já é " + newRole).build();
        }

        if (!isValidRole(newRole)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Nova role inválida: " + newRole).build();
        }

        if (!Permission.canChangeRole(requesterRole, currentRole, newRole)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Não tens permissão para alterar esta role.").build();
        }

        ds.update(Entity.newBuilder(target).set("user_role", newRole).build());
        updateActiveSessionsRole(targetUsername, newRole);

        LOG.info("Role de " + targetUsername + " alterada para " + newRole);
        return Response.ok("Role atualizada com sucesso.").build();
    }

    private static boolean isValidRole(String role) {
        return role.equals("ENDUSER") || role.equals("PARTNER") ||
               role.equals("BACKOFFICE") || role.equals("ADMIN");
    }

    private static void updateActiveSessionsRole(String username, String newRole) {
        List<Entity> sessions = DatastoreQueries.getActiveSessions(username);
        for (Entity session : sessions) {
            ds.update(Entity.newBuilder(session).set("session_role", newRole).build());
        }
    }
}
