package pt.unl.fct.apdc.assignment.resources;

import java.util.Optional;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import pt.unl.fct.apdc.assignment.util.Permission;
import pt.unl.fct.apdc.assignment.util.data.ChangeRoleData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQuery;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;


/*
 * postman input
 * {
 * "tokenID": "SEU_TOKEN_AQUI",
 * "targetUser": "utilizadoralvo",  // username ou email
 * "newRole": "PARTNER"
 *}
 *
 */
@Path("/changerole")
@Produces(MediaType.APPLICATION_JSON)
public class ChangeRoleResource {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final Gson g = new Gson();

    private static final Logger LOG = Logger.getLogger(ChangeRoleData.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeRole(ChangeRoleData data) {
        // Validar token do requester
        Optional<Entity> tokenOpt = DatastoreQuery.getTokenEntityByID(data.requesterID);
        if (tokenOpt.isEmpty() || !DatastoreToken.isTokenValid(tokenOpt.get())) {
            return Response.status(Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }

        Entity tokenEntity = tokenOpt.get();
        String requesterUsername = DatastoreToken.getUsername(tokenEntity);
        String requesterRole = DatastoreToken.getRole(tokenEntity).toUpperCase();
        String newRole = data.newRole.toUpperCase();

        // Obter utilizador alvo (pode ser ele mesmo ou outro)
        Optional<Entity> targetOpt = DatastoreQuery.getUserByUsername(data.targetUser);
        if (targetOpt.isEmpty()) {
            targetOpt = DatastoreQuery.getUserByEmail(data.targetUser);
            if (targetOpt.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("Utilizador alvo não existe.").build();
                
            }
        }

        Entity target = targetOpt.get();
		String targetUsername = target.getKey().getName(); // username verdadeiro
        String currentRole = target.getString("user_role").toUpperCase();

        // Verificar permissões
        if (!Permission.canChangeRole(requesterRole, currentRole, newRole)) {
            return Response.status(Status.FORBIDDEN).entity("Permissão negada.").build();
        }

        // Atualizar no Datastore
        Entity updatedUser = Entity.newBuilder(target)
                .set("user_role", newRole)
                .build();

        datastore.update(updatedUser);

        JsonObject result = new JsonObject();
        result.addProperty("message", "Role de " + targetUsername + " alterado para " + newRole + " com sucesso.");
        LOG.info("Role de " + targetUsername + " alterado para " + newRole + " com sucesso.");
        return Response.ok(result.toString()).build();
    }

    /*
    * postman input
    * {
    * "requesterID": "requesterID",
    * "targetUser": "utilizadoralvo",  // username ou email
    * "newRole": "PARTNER"
    *}
    */
    // Pode ser nome, email ou tokenID
    @Path("/latest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeRoleWithLatestSession(ChangeRoleData data) {
        Optional<Entity> tokenOpt = DatastoreQuery.getTokenEntityByID(data.requesterID);

        Entity tokenEntity = null;

        if (tokenOpt.isPresent() && DatastoreToken.isTokenValid(tokenOpt.get())) {
            tokenEntity = tokenOpt.get();
        } else {
            // Não é tokenID ou é username ou email, tentar obter sessão mais recente válida
            Optional<Entity> userOpt = DatastoreQuery.getUserByUsername(data.requesterID);
            if (userOpt.isEmpty()) {
                userOpt = DatastoreQuery.getUserByEmail(data.requesterID);
                if (userOpt.isEmpty()) {
                    return Response.status(Status.NOT_FOUND).entity("Requester não encontrado.").build();
                }
            }

            String requesterUsername = userOpt.get().getKey().getName();
            tokenOpt = DatastoreToken.getLatestValidSession(requesterUsername);
            if (tokenOpt.isEmpty()) {
                return Response.status(Status.UNAUTHORIZED).entity("Nenhuma sessão válida encontrada.").build();
            }
            tokenEntity = tokenOpt.get();
        }

        String requesterRole = DatastoreToken.getRole(tokenEntity).toUpperCase();
        String newRole = data.newRole.toUpperCase();

        // Obter utilizador alvo
        Optional<Entity> targetOpt = DatastoreQuery.getUserByUsername(data.targetUser);
        if (targetOpt.isEmpty()) {
            targetOpt = DatastoreQuery.getUserByEmail(data.targetUser);
            if (targetOpt.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity("Utilizador alvo não existe.").build();
            }
        }

        Entity target = targetOpt.get();
        String targetUsername = target.getKey().getName();
        String currentRole = target.getString("user_role").toUpperCase();

        if (!Permission.canChangeRole(requesterRole, currentRole, newRole)) {
            return Response.status(Status.FORBIDDEN).entity("Permissão negada.").build();
        }

        Entity updatedUser = Entity.newBuilder(target)
                .set("user_role", newRole)
                .build();

        datastore.update(updatedUser);

        JsonObject result = new JsonObject();
        result.addProperty("message", "Role de " + targetUsername + " alterado para " + newRole + " com sucesso.");
        LOG.info("Role de " + targetUsername + " alterado para " + newRole + " com sucesso.");
        return Response.ok(result.toString()).build();
    }
}
