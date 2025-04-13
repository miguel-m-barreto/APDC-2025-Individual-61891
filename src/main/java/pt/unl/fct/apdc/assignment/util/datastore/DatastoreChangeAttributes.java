package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.apdc.assignment.util.Permission;
import pt.unl.fct.apdc.assignment.util.data.ChangeAttributesData;

import java.util.Optional;

public class DatastoreChangeAttributes {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Response processAttributeUpdate(ChangeAttributesData data, String requesterUsername, String requesterRole) {
        Optional<Entity> targetOpt = DatastoreQueries.getUserByUsername(data.targetUsername);
        if (targetOpt.isEmpty()) {
            return Response.status(Status.NOT_FOUND).entity("Utilizador alvo não existe.").build();
        }

        Entity original = targetOpt.get();
        String targetUsername = original.getKey().getName();
        String targetRole = original.getString("user_role");
        String targetState = original.getString("user_account_state");
        boolean targetIsActivated = targetState.equalsIgnoreCase("ATIVADA");

        // Valida se está a tentar mudar control fields
        boolean changingControlFields = data.role != null || data.state != null;
        boolean changingUserIdFields = data.username != null || data.email != null || data.name != null;

        if (!Permission.canChangeAttributes(
                requesterRole, requesterUsername, targetUsername,
                targetRole, targetIsActivated,
                changingControlFields, changingUserIdFields
        )) {
            return Response.status(Status.FORBIDDEN).entity("Não tens permissão para editar esta conta.").build();
        }

        // Atualizar entidade
        Entity.Builder builder = Entity.newBuilder(original);

        if (data.name != null) builder.set("user_name", data.name);
        if (data.phone != null) builder.set("user_phone", data.phone);
        if (data.address != null) builder.set("user_address", data.address);
        if (data.job != null) builder.set("user_job", data.job);
        if (data.employer != null) builder.set("user_employer", data.employer);
        if (data.nif != null) builder.set("user_nif", data.nif);
        if (data.cc != null) builder.set("user_cc", data.cc);
        if (data.profile != null) builder.set("user_profile", data.profile);
        if (data.employer_nif != null) builder.set("user_employer_nif", data.employer_nif);
        if (data.photoURL != null) builder.set("user_photo_url", data.photoURL);

        // Só ADMIN pode mudar role e estado
        if (requesterRole.equalsIgnoreCase("ADMIN")) {
            if (data.role != null) builder.set("user_role", data.role);
            if (data.state != null) builder.set("user_account_state", data.state);
        }

        datastore.update(builder.build());
        return Response.ok("Atributos atualizados com sucesso.").build();
    }
}
