package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.apdc.assignment.util.data.WorkSheetData;
import pt.unl.fct.apdc.assignment.util.data.WorkSheetListData;
import pt.unl.fct.apdc.assignment.util.datastore.*;

import java.util.Optional;

@Path("/worksheet")
@Produces(MediaType.APPLICATION_JSON)
public class WorkSheetResource {
    
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(WorkSheetData data) {
        if (data == null || data.requesterID == null || data.requesterID.isBlank())
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();

        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();


        Entity token = tokenOpt.get();
        String role = DatastoreToken.getRole(token);
        String username = DatastoreToken.getUsername(token);

        return DatastoreWorkSheet.createWorkSheet(data, role, username);
    }
    
    @POST
    @Path("/adjudicate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response adjudicate(WorkSheetData data) {
        if (data == null || data.requesterID == null || data.requesterID.isBlank() || data.token == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();

        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();

        Entity token = tokenOpt.get();
        String role = DatastoreToken.getRole(token);
        String username = DatastoreToken.getUsername(token);

        return DatastoreWorkSheet.adjudicateWorkSheet(data, role, username);
    }

    @POST
    @Path("/progress")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProgress(WorkSheetData data) {
        if (data == null || data.requesterID == null || data.requesterID.isBlank())
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();

        // Verificar sessão
        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();
        }
        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID)) 
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida ou expirada.").build();

        Entity token = tokenOpt.get();
        String role = DatastoreToken.getRole(token);
        String username = DatastoreToken.getUsername(token);

        return DatastoreWorkSheet.updateProgress(data, role, username);
    }

    @POST
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listWorksheets(WorkSheetData data) {
        if (data == null || data.requesterID == null || data.requesterID.isBlank() || data.token == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();

        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty() || !DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida.").build();

        String role = DatastoreToken.getRole(tokenOpt.get());
        if (!role.equals("BACKOFFICE") && !role.equals("ADMIN"))
            return Response.status(Response.Status.FORBIDDEN).entity("Permissão negada.").build();

        return DatastoreWorkSheet.listWorksheets();
    }

    @POST
    @Path("/list/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listByState(WorkSheetListData data) {
        if (data == null || data.requesterID == null || data.token == null || data.status == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados em falta.").build();

        Optional<Entity> tokenOpt = DatastoreQueries.getTokenEntityByID(data.token);
        if (tokenOpt.isEmpty())
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida.").build();

        if (!DatastoreToken.isValidTokenForUser(tokenOpt.get(), data.requesterID))
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token inválido.").build();

        Entity token = tokenOpt.get();
        String role = DatastoreToken.getRole(token);
        String username = DatastoreToken.getUsername(token);

        return DatastoreWorkSheet.listByState(data.status, role, username);
    }




}
