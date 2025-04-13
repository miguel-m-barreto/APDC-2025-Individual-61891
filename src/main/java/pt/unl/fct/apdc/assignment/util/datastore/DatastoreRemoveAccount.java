package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import com.google.gson.JsonObject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assignment.util.Permission;

import java.util.Optional;
import java.util.logging.Logger;

public class DatastoreRemoveAccount {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(DatastoreRemoveAccount.class.getName());

    public static Response processAccountRemoval(String requesterRole, String targetUser) {
        Optional<Entity> targetOpt = DatastoreQuery.getUserByUsername(targetUser);
        if (targetOpt.isEmpty()) {
            targetOpt = DatastoreQuery.getUserByEmail(targetUser);
            if (targetOpt.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("Conta a remover não encontrada.").build();
            }
        }

        Entity target = targetOpt.get();
        String targetUsername = target.getKey().getName();
        String targetRole = target.getString("user_role").toUpperCase();

        if (!Permission.canRemoveAccount(requesterRole, targetRole)) {
            return Response.status(Status.FORBIDDEN).entity("Sem permissões para remover esta conta.").build();
        }

        // Remove todas as sessões e a conta
        DatastoreToken.deleteAllSessions(targetUsername);
        datastore.delete(target.getKey());

        JsonObject result = new JsonObject();
        result.addProperty("message", "Conta de " + targetUsername + " removida com sucesso.");
        LOG.info("Conta de " + targetUsername + " removida por " + requesterRole);
        return Response.ok(result.toString()).build();
    }
}
