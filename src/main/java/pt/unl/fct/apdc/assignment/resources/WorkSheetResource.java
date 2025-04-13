package pt.unl.fct.apdc.assignment.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import pt.unl.fct.apdc.assignment.util.data.WorkSheetData;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreQueries;
import pt.unl.fct.apdc.assignment.util.datastore.DatastoreToken;

import java.util.Optional;


@Path("/worksheet")
@Produces(MediaType.APPLICATION_JSON)
public class WorkSheetResource {

    private static final String EMPTY_STRING = "NOT DEFINED";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateWorkSheet(WorkSheetData data) {
        if (data == null || data.requesterID == null || data.requesterID.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Dados inválidos.").build();
        }

        Optional<Entity> tokenOpt = DatastoreQueries.getToken(data.requesterID);
        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Sessão inválida.").build();
        }

        Entity token = tokenOpt.get();
        String requesterRole = DatastoreToken.getRole(token);
        String requesterUsername = DatastoreToken.getUsername(token);

        KeyFactory keyFactory = DatastoreOptions.getDefaultInstance().getService()
            .newKeyFactory().setKind("WorkSheet");
        Key workKey = keyFactory.newKey(data.reference);

        Datastore ds = DatastoreOptions.getDefaultInstance().getService();

        if (requesterRole.equalsIgnoreCase("BACKOFFICE")) {
            if (!data.isValidForBackofficeCreate()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Campos obrigatórios em falta.").build();
            }

            Entity.Builder builder = Entity.newBuilder(workKey)
                    .set("reference", data.reference)
                    .set("description", data.description)
                    .set("targetType", data.targetType)
                    .set("adjudicationStatus", data.adjudicationStatus);

            if ("ADJUDICADO".equalsIgnoreCase(data.adjudicationStatus)) {
                builder.set("adjudicationDate", data.adjudicationDate == null ? EMPTY_STRING : data.adjudicationDate)
                       .set("startDate", data.startDate == null ? EMPTY_STRING : data.startDate)
                       .set("endDate", data.endDate == null ? EMPTY_STRING : data.endDate)
                       .set("partnerAccount", data.partnerAccount == null ? EMPTY_STRING : data.partnerAccount)
                       .set("companyName", data.companyName == null ? EMPTY_STRING : data.companyName)
                       .set("companyNIF", data.companyNIF == null ? EMPTY_STRING : data.companyNIF);
            }

            ds.put(builder.build());
            return Response.ok("Folha de obra criada/atualizada com sucesso.").build();
        }

        if (requesterRole.equalsIgnoreCase("PARTNER")) {
            if (!data.isValidForPartnerUpdate()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Campos obrigatórios para PARTNER em falta.").build();
            }

            Entity ws = ds.get(workKey);
            if (ws == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Folha de obra não encontrada.").build();
            }

            if (!requesterUsername.equals(ws.getString("partnerAccount"))) {
                return Response.status(Response.Status.FORBIDDEN).entity("Não tens permissão para editar esta obra.").build();
            }

            Entity updated = Entity.newBuilder(ws)
                    .set("workState", data.workState)
                    .set("observations", data.observations == null ? "NOT DEFINED" : data.observations)
                    .build();

            ds.put(updated);
            return Response.ok("Estado da obra atualizado com sucesso.").build();
        }

        return Response.status(Response.Status.FORBIDDEN).entity("Permissão negada.").build();
    }
}
