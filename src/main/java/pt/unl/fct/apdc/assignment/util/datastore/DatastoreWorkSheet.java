package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import pt.unl.fct.apdc.assignment.util.data.WorkSheetData;

import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class DatastoreWorkSheet {

    private static final Datastore ds = DatastoreOptions.getDefaultInstance().getService();
    private static final String EMPTY_STRING = "NOT DEFINED";

    public static Response process(WorkSheetData data, String role, String username) {
        KeyFactory keyFactory = ds.newKeyFactory().setKind("WorkSheet");
        Key workKey = keyFactory.newKey(data.reference);

        if ((role.equals("BACKOFFICE") || role.equals("ADMIN")) && data.isValidForBackofficeCreate()) {
            String newToken = generateWorkToken(data);
            Entity existing = ds.get(workKey);

            if (existing != null && existing.contains("worksheet_token")) {
                String existingToken = existing.getString("worksheet_token");
                if (existingToken.equals(newToken)) {
                    return Response.status(Response.Status.CONFLICT).entity("Já existe uma folha de obra idêntica.").build();
                }
            }

            Entity.Builder builder = Entity.newBuilder(workKey)
                .set("reference", data.reference)
                .set("description", data.description)
                .set("targetType", data.targetType)
                .set("adjudicationStatus", data.adjudicationStatus)
                .set("worksheet_token", newToken);

            if ("ADJUDICADO".equalsIgnoreCase(data.adjudicationStatus)) {
                builder.set("adjudicationDate", data.adjudicationDate != null ? data.adjudicationDate : EMPTY_STRING)
                       .set("startDate", data.startDate != null ? data.startDate : EMPTY_STRING)
                       .set("endDate", data.endDate != null ? data.endDate : EMPTY_STRING)
                       .set("partnerAccount", data.partnerAccount != null ? data.partnerAccount : EMPTY_STRING)
                       .set("companyName", data.companyName != null ? data.companyName : EMPTY_STRING)
                       .set("companyNIF", data.companyNIF != null ? data.companyNIF : EMPTY_STRING);
            }

            ds.put(builder.build());
            return Response.ok("Folha de obra criada/atualizada com sucesso.").build();
        }

        if ((role.equals("PARTNER") || role.equals("ADMIN")) && data.isValidForPartnerUpdate()) {
            Entity ws = ds.get(workKey);
            if (ws == null) return Response.status(Response.Status.NOT_FOUND).entity("Folha de obra não encontrada.").build();
            if (!username.equals(ws.getString("partnerAccount")))
                return Response.status(Response.Status.FORBIDDEN).entity("Não tens permissão para editar esta obra.").build();

            Entity updated = Entity.newBuilder(ws)
                    .set("workState", data.workState)
                    .set("observations", data.observations != null ? data.observations : EMPTY_STRING)
                    .build();

            ds.put(updated);
            return Response.ok("Estado da obra atualizado com sucesso.").build();
        }

        return Response.status(Response.Status.FORBIDDEN).entity("Permissão negada.").build();
    }

    private static String generateWorkToken(WorkSheetData data) {
        try {
            String input = (data.reference + data.description + data.targetType +
                            data.adjudicationStatus + data.adjudicationDate + data.startDate +
                            data.endDate + data.partnerAccount + data.companyName + data.companyNIF).trim();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "";
        }
    }
}
