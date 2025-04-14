package pt.unl.fct.apdc.assignment.util.handler;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.data.ChangeAttributesData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.validation.ChangeAttributesValidator;

import static pt.unl.fct.apdc.assignment.util.StringUtil.normalizeEmail;
import static pt.unl.fct.apdc.assignment.util.StringUtil.normalizeProfileType;

import java.util.Optional;

public class ChangeAttributesHandler {
    private static final String EMPTY_STRING = "NOT DEFINED";

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public static Response applyChangesForEndUser(ChangeAttributesData data, Entity requester) {
        return applyChanges(data, requester, "ENDUSER");
    }

    public static Response applyChangesForBackoffice(ChangeAttributesData data, Entity requester) {
        return applyChanges(data, requester, "BACKOFFICE");
    }

    public static Response applyChangesForAdmin(ChangeAttributesData data, Entity requester) {
        return applyChanges(data, requester, "ADMIN");
    }

    private static Response applyChanges(ChangeAttributesData data, Entity requester, String role) {
        String requesterUsername = requester.getString("session_username");
    
        if (data.targetUsername == null || data.targetUsername.isBlank())
            data.targetUsername = requesterUsername;
    
        Optional<Entity> targetOpt = DatastoreQueries.getUserByUsernameOrEmail(data.targetUsername);
        if (targetOpt.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).entity("Utilizador a alterar não existe.").build();
    
        Entity original = targetOpt.get();
    
        if (!ChangeAttributesValidator.hasPermission(requester, original, data)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Sem permissão para alterar estes atributos.").build();
        }
    
        boolean isAdmin = role.equalsIgnoreCase("ADMIN");
        boolean changingKey = isAdmin && data.username != null && !data.username.equals(original.getKey().getName());
    
        String newEmail = data.email != null ? normalizeEmail(data.email) : original.getString("user_email");
        String newProfile = data.profile != null ? normalizeProfileType(data.profile) : original.getString("user_profile");
    
        Entity.Builder builder;
        boolean hasChanges = false;
    
        // 🔁 Se mudar a chave (username), construir nova entidade
        if (changingKey && role.equalsIgnoreCase("ADMIN")) {
            if (DatastoreQueries.getUserByUsername(data.username).isPresent()) {
                return Response.status(Response.Status.CONFLICT).entity("Novo username já está em uso.").build();
            }
    
            builder = Entity.newBuilder(original);
            builder.setKey(datastore.newKeyFactory().setKind("User").newKey(data.username));
            //buildNew(original, data, data.username);
            datastore.delete(original.getKey());
            hasChanges = true;
        } else {
            builder = Entity.newBuilder(original);
        }
    
        hasChanges |= maybeSet(builder, original, "user_name", data.name);
        hasChanges |= maybeSet(builder, original, "user_phone", data.phone);
        hasChanges |= maybeSet(builder, original, "user_address", data.address);
        hasChanges |= maybeSet(builder, original, "user_job", data.job);
        hasChanges |= maybeSet(builder, original, "user_employer", data.employer);
        hasChanges |= maybeSet(builder, original, "user_nif", data.nif);
        hasChanges |= maybeSet(builder, original, "user_cc", data.cc);
        hasChanges |= maybeSet(builder, original, "user_profile", newProfile);
        hasChanges |= maybeSet(builder, original, "user_employer_nif", data.employer_nif);
        hasChanges |= maybeSet(builder, original, "user_photo_url", data.photoURL);
    
        if (role.equals("ADMIN") || role.equals("BACKOFFICE")) {
            hasChanges |= maybeSet(builder, original, "user_role", data.role);
            hasChanges |= maybeSet(builder, original, "user_account_state", data.state);
    
            if (isAdmin && data.email != null && !data.email.equals(original.getString("user_email"))) {
                if (DatastoreQueries.getUserByEmail(newEmail).isPresent()) {
                    return Response.status(Response.Status.CONFLICT).entity("Email já está em uso.").build();
                }
                builder.set("user_email", newEmail);
                hasChanges = true;
            }
        }
        
    
        if (!hasChanges) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Nenhum atributo foi alterado.").build();
        }
    
        datastore.put(builder.build());
        return Response.ok("Atributos atualizados com sucesso.").build();
    }
    

    private static boolean maybeSet(Entity.Builder builder, Entity original, String field, String newValue) {
        if (newValue != null && (!original.contains(field) || !original.getString(field).equals(newValue))) {
            builder.set(field, newValue);
            return true;
        }
        return false;
    }


    private static Entity.Builder buildNew(Entity original, ChangeAttributesData data, String newUsername) {
        Key newKey = datastore.newKeyFactory().setKind("User").newKey(newUsername);

        return Entity.newBuilder(newKey)
                .set("user_name", original.contains("user_name") ? original.getString("user_name") : EMPTY_STRING)
                .set("user_email", original.contains("user_email") ? original.getString("user_email") : EMPTY_STRING)
                .set("user_phone", original.contains("user_phone") ? original.getString("user_phone") : EMPTY_STRING)
                .set("user_address", original.contains("user_address") ? original.getString("user_address") : EMPTY_STRING)
                .set("user_job", original.contains("user_job") ? original.getString("user_job") : EMPTY_STRING)
                .set("user_employer", original.contains("user_employer") ? original.getString("user_employer") : EMPTY_STRING)
                .set("user_nif", original.contains("user_nif") ? original.getString("user_nif") : EMPTY_STRING)
                .set("user_cc", original.contains("user_cc") ? original.getString("user_cc") : EMPTY_STRING)
                .set("user_profile", original.contains("user_profile") ? original.getString("user_profile") : EMPTY_STRING)
                .set("user_employer_nif", original.contains("user_employer_nif") ? original.getString("user_employer_nif") : EMPTY_STRING)
                .set("user_photo_url", original.contains("user_photo_url") ? original.getString("user_photo_url") : EMPTY_STRING)
                //.set("user_pwd", original.getString("user_pwd")) // Password é alterada aqui
                .set("user_role", original.contains("user_role") ? original.getString("user_role") : EMPTY_STRING)
                .set("user_account_state", original.contains("user_account_state") ? original.getString("user_account_state") : EMPTY_STRING)
                .set("user_creation_time", original.contains("user_creation_time") ? original.getLong("user_creation_time") : System.currentTimeMillis());
    }

    
}
