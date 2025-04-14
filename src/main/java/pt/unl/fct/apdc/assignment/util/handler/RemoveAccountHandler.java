package pt.unl.fct.apdc.assignment.util.handler;

import com.google.cloud.datastore.*;
import com.google.gson.JsonObject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assignment.util.data.RemoveAccountData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import pt.unl.fct.apdc.assignment.util.validation.RemoveAccountValidator;

import java.util.Optional;

public class RemoveAccountHandler {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Response removeAccount(RemoveAccountData data, Entity requester) {
        //String requesterRole = requester.getString("session_role");
        //String requesterUsername = requester.getString("session_username");

        Optional<Entity> targetOpt = DatastoreQueries.getUserByUsernameOrEmail(data.targetUser);
        if (targetOpt.isEmpty()) {
            return Response.status(Status.NOT_FOUND).entity("Conta a remover não encontrada.").build();
        }

        Entity target = targetOpt.get();

        if (!RemoveAccountValidator.hasPermission(requester, target)) {
            return Response.status(Status.FORBIDDEN).entity("Sem permissões para remover esta conta.").build();
        }

        DatastoreToken.deleteAllSessions(target.getKey().getName());
        datastore.delete(target.getKey());

        JsonObject result = new JsonObject();
        result.addProperty("message", "Conta de " + target.getKey().getName() + " removida com sucesso.");
        return Response.ok(result.toString()).build();
    }
}
