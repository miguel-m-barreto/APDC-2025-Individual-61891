package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.apdc.assignment.util.data.WorkSheetData;
import pt.unl.fct.apdc.assignment.util.datastore.*;

import java.util.Optional;

@Path("/worksheet")
@Produces(MediaType.APPLICATION_JSON)
public class WorkSheetResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateWorkSheet(WorkSheetData data) {
        if (data == null || data.requesterID == null || data.requesterID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();
        }

        Optional<Entity> tokenOpt = DatastoreQueries.getToken(data.requesterID);
        if (tokenOpt.isEmpty())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida.").build();

        Entity token = tokenOpt.get();
        String role = DatastoreToken.getRole(token);
        String username = DatastoreToken.getUsername(token);

        return DatastoreWorkSheet.process(data, role, username);
    }
}
