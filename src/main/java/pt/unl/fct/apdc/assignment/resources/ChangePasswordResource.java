package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.apdc.assignment.util.data.ChangePasswordData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Optional;

@Path("/changepassword")
@Produces(MediaType.APPLICATION_JSON)
public class ChangePasswordResource {

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(ChangePasswordData data) {
        if (!data.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();
        }

        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();

        Entity tokenEntity = tokenOpt.get();
        String username = DatastoreToken.getUsername(tokenEntity);

        // Verificar se está a tentar mudar a sua própria password
        if (!username.equals(data.requesterID)) {
            return Response.status(Response.Status.FORBIDDEN).entity("Só podes alterar a tua própria password.").build();
        }

        Optional<Entity> userOpt = DatastoreQueries.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Utilizador não encontrado.").build();
        }

        Entity user = userOpt.get();
        String storedHash = user.getString("user_pwd");

        if (!storedHash.equals(DigestUtils.sha512Hex(data.oldPassword))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Password atual incorreta.").build();
        }

        if (isSamePassword(storedHash, data.newPassword)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A nova password não pode ser igual à antiga.").build();
            
        }

        Entity updatedUser = Entity.newBuilder(user)
                .set("user_pwd", DigestUtils.sha512Hex(data.newPassword))
                .build();

        datastore.update(updatedUser);

        return Response.ok("Password alterada com sucesso.").build();
    }

    private boolean isSamePassword(String oldHashed, String newPlain) {
        return oldHashed.equals(DigestUtils.sha512Hex(newPlain));
    }
}
