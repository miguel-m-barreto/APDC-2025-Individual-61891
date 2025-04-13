package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import com.google.gson.JsonObject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assignment.util.Permission;

import java.util.Optional;
import java.util.logging.Logger;

public class DatastoreChangeState {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Logger LOG = Logger.getLogger(DatastoreChangeState.class.getName());

    public static Response processStateChange(String requesterRole, String targetUser, String newState) {
        try {
            Optional<Entity> targetOpt = DatastoreQueries.getUserByUsername(targetUser);
            if (targetOpt.isEmpty()) {
                targetOpt = DatastoreQueries.getUserByEmail(targetUser);
                if (targetOpt.isEmpty()) {
                    return Response.status(Status.NOT_FOUND).entity("Utilizador alvo não existe.").build();
                }
            }

            Entity target = targetOpt.get();
            String currentState = target.getString("user_account_state").toUpperCase();
            newState = checkSpelling(newState.trim().toUpperCase());

            if (newState.equals(currentState)) {
                return Response.status(Status.BAD_REQUEST).entity("Estado não alterado.\nO estado atual é o mesmo que o novo estado.").build();
            }

            if (!isStateValid(newState)) {
                return Response.status(Status.BAD_REQUEST).entity("Estado inválido.").build();
            }

            if (!Permission.canChangeState(requesterRole, currentState, newState)) {
                return Response.status(Status.FORBIDDEN).entity("Permissão negada para alterar o estado.").build();
            }

            try {
                Entity updatedUser = Entity.newBuilder(target)
                        .set("user_account_state", newState)
                        .build();

                datastore.update(updatedUser);
            } catch (DatastoreException e) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Erro ao atualizar o estado da conta.").build();
            }

            if (newState.equals("SUSPENSA") || newState.equals("DESATIVADA")) {
                DatastoreToken.deleteAllSessions(target.getKey().getName());
            }

            JsonObject result = new JsonObject();
            result.addProperty("message", "Estado de conta de " + target.getKey().getName() + " alterado para " + newState);
            LOG.info("Estado de conta de " + target.getKey().getName() + " alterado para " + newState);
            return Response.ok(result.toString()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Erro ao alterar o estado da conta.").build();
        } catch (DatastoreException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Erro ao comunicar com o Datastore.").build();
        }
    }

    private static boolean isStateValid(String newState) {
        return (newState.equals("ATIVADA") || newState.equals("DESATIVADA") || newState.equals("SUSPENSA"));
    }

    private static String checkSpelling(String state) {
        switch (state) {
            case "ATIVADO":
            case "ATIVADA":
                return "ATIVADA";
            case "DESATIVADO":
            case "DESATIVADA":
                return "DESATIVADA";
            case "SUSPENSO":
            case "SUSPENSA":
                return "SUSPENSA";
            default:
                return null;
        }
    }
}
