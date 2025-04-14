package pt.unl.fct.apdc.assignment.util.handler;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.unl.fct.apdc.assignment.util.data.ChangeAccountStateData;
import pt.unl.fct.apdc.assignment.util.Permission;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;

import java.util.Optional;
import java.util.logging.Logger;

public class ChangeStateHandler {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(ChangeStateHandler.class.getName());

    public static Response changeStateForAdmin(ChangeAccountStateData data, Entity requester) {
        String requesterRole = requester.getString("session_role").toUpperCase();
        if (!"ADMIN".equals(requesterRole))
            return Response.status(Status.FORBIDDEN).entity("Apenas ADMIN pode aceder a este endpoint.").build();

        return processStateChange(data, requesterRole);
    }

    public static Response changeStateForBackoffice(ChangeAccountStateData data, Entity requester) {
        String requesterRole = requester.getString("session_role").toUpperCase();
        if (!"BACKOFFICE".equals(requesterRole) || "ADMIN".equals(requesterRole))
            return Response.status(Status.FORBIDDEN).entity("Apenas BACKOFFICE pode aceder a este endpoint.").build();

        return processStateChange(data, requesterRole);
    }

    private static Response processStateChange(ChangeAccountStateData data, String requesterRole) {
        Optional<Entity> targetOpt = DatastoreQueries.getUserByUsernameOrEmail(data.targetUser);
        if (targetOpt.isEmpty())
            return Response.status(Status.NOT_FOUND).entity("Utilizador alvo não existe.").build();

        Entity target = targetOpt.get();
        String targetUsername = target.getKey().getName();
        String currentState = target.getString("user_account_state").toUpperCase();
        String newState = normalizeState(data.newState);

        if (!isStateValid(newState))
            return Response.status(Status.BAD_REQUEST).entity("Estado inválido: " + newState).build();

        if (newState.equals(currentState))
            return Response.status(Status.BAD_REQUEST).entity("Estado já está definido como " + currentState).build();

        if (!Permission.canChangeState(requesterRole, currentState, newState))
            return Response.status(Status.FORBIDDEN).entity("Sem permissão para alterar este estado.").build();

        Entity updated = Entity.newBuilder(target)
                .set("user_account_state", newState)
                .build();

        datastore.update(updated);

        if (newState.equals("SUSPENSA") || newState.equals("DESATIVADA"))
            DatastoreToken.deleteAllSessions(targetUsername);

        LOG.info("Estado de " + targetUsername + " alterado para " + newState + " por " + requesterRole);
        return Response.ok("Estado alterado com sucesso para " + newState + ".").build();
    }

    private static String normalizeState(String state) {
        switch (state.trim().toUpperCase()) {
            case "ATIVADO": case "ATIVADA":
                return "ATIVADA";
            case "DESATIVADO": case "DESATIVADA":
                return "DESATIVADA";
            case "SUSPENSO": case "SUSPENSA":
                return "SUSPENSA";
            default:
                return "INVALID";
        }
    }

    private static boolean isStateValid(String state) {
        return state.equals("ATIVADA") || state.equals("DESATIVADA") || state.equals("SUSPENSA");
    }
}
