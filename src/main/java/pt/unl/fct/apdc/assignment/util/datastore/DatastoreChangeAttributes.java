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
        if (data.targetUsername == null || data.targetUsername.isBlank()) {
            data.targetUsername = requesterUsername;
            
        }
        Optional<Entity> targetOpt = DatastoreQueries.getUserByUsernameOrEmail(data.targetUsername);
        if (targetOpt.isEmpty()) {
            return Response.status(Status.NOT_FOUND).entity("Utilizador a alterar não existe.").build();
        }

        Entity original = targetOpt.get();
        String targetUsername = original.getKey().getName();
        String targetRole = original.getString("user_role");
        String targetState = original.getString("user_account_state");
        boolean targetIsActivated = targetState.equalsIgnoreCase("ATIVADA");

        boolean changingControlFields = data.role != null || data.state != null;
        boolean changingUserIdFields = data.username != null || data.email != null;

        if (!Permission.canChangeAttributes(
                requesterRole, requesterUsername, targetUsername,
                targetRole, targetIsActivated,
                changingControlFields, changingUserIdFields
        )) {
            if (targetUsername.equals(requesterUsername)) {
                return Response.status(Status.FORBIDDEN).entity("Não tens permissão para editar um ou mais desses atributos.").build();
            }
            return Response.status(Status.FORBIDDEN).entity("Não tens permissão para editar esta conta.").build();
        }

        boolean changingKey = requesterRole.equalsIgnoreCase("ADMIN") && data.username != null && !data.username.equals(targetUsername);
        String newUsername = changingKey ? data.username : targetUsername;
        Entity.Builder builder;
        boolean hasChanges = false;

        if (changingKey) {
            builder = buildWithNewKey(original, data, newUsername);
            hasChanges = true;
            datastore.delete(original.getKey());
        } else {
            builder = Entity.newBuilder(original);

            hasChanges |= maybeSet(builder, original, "user_name", data.name);
            hasChanges |= maybeSet(builder, original, "user_phone", data.phone);
            hasChanges |= maybeSet(builder, original, "user_address", data.address);
            hasChanges |= maybeSet(builder, original, "user_job", data.job);
            hasChanges |= maybeSet(builder, original, "user_employer", data.employer);
            hasChanges |= maybeSet(builder, original, "user_nif", data.nif);
            hasChanges |= maybeSet(builder, original, "user_cc", data.cc);
            hasChanges |= maybeSet(builder, original, "user_profile", data.profile);
            hasChanges |= maybeSet(builder, original, "user_employer_nif", data.employer_nif);
            hasChanges |= maybeSet(builder, original, "user_photo_url", data.photoURL);
        }
        if (requesterRole.equalsIgnoreCase("ADMIN") || requesterRole.equalsIgnoreCase("BACKOFFICE")) {
            hasChanges |= maybeSet(builder, original, "user_role", data.role);
            hasChanges |= maybeSet(builder, original, "user_account_state", data.state);

            if (requesterRole.equalsIgnoreCase("ADMIN")) {
                if (data.email != null && !data.email.equals(original.getString("user_email"))) {
                    if (DatastoreQueries.getUserByEmail(data.email).isPresent()) {
                        return Response.status(Status.CONFLICT).entity("Email já está em uso.").build();
                    }
                    builder.set("user_email", data.email);
                    hasChanges = true;
                }

                /*
                if (data.password != null && !data.password.isBlank()) {
                    builder.set("user_pwd", hashPassword(data.password));
                    hasChanges = true;
                }*/
            }
        }

        if (!hasChanges) {
            return Response.status(Status.BAD_REQUEST).entity("Nenhum atributo foi alterado.").build();
        }

        datastore.put(builder.build());
        return Response.ok("Atributos atualizados com sucesso.").build();
    }

    
    private static Entity.Builder buildWithNewKey(Entity original, ChangeAttributesData data, String newUsername) {
        Key newKey = datastore.newKeyFactory().setKind("User").newKey(newUsername);

        return Entity.newBuilder(newKey)
                .set("user_name", data.name != null ? data.name : original.getString("user_name"))
                .set("user_email", data.email != null ? data.email : original.getString("user_email"))
                .set("user_phone", data.phone != null ? data.phone : getOrDefault(original, "user_phone"))
                .set("user_address", data.address != null ? data.address : getOrDefault(original, "user_address"))
                .set("user_job", data.job != null ? data.job : getOrDefault(original, "user_job"))
                .set("user_employer", data.employer != null ? data.employer : getOrDefault(original, "user_employer"))
                .set("user_nif", data.nif != null ? data.nif : getOrDefault(original, "user_nif"))
                .set("user_cc", data.cc != null ? data.cc : getOrDefault(original, "user_cc"))
                .set("user_profile", data.profile != null ? data.profile : getOrDefault(original, "user_profile"))
                .set("user_employer_nif", data.employer_nif != null ? data.employer_nif : getOrDefault(original, "user_employer_nif"))
                .set("user_photo_url", data.photoURL != null ? data.photoURL : getOrDefault(original, "user_photo_url"))
                //.set("user_pwd", data.password != null ? hashPassword(data.password) : original.getString("user_pwd"))
                .set("user_role", data.role != null ? data.role : original.getString("user_role"))
                .set("user_account_state", data.state != null ? data.state : original.getString("user_account_state"))
                .set("user_creation_time", original.getLong("user_creation_time"));
    }

    private static boolean maybeSet(Entity.Builder builder, Entity original, String field, String newValue) {
        if (newValue != null && (!original.contains(field) || !original.getString(field).equals(newValue))) {
            builder.set(field, newValue);
            return true;
        }
        return false;
    }

    
    private static String getOrDefault(Entity entity, String field) {
        return entity.contains(field) ? entity.getString(field) : "NOT DEFINED";
    }

    private static String hashPassword(String raw) {
        return org.apache.commons.codec.digest.DigestUtils.sha512Hex(raw);
    }
}
