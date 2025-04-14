package pt.unl.fct.apdc.assignment.util.datastore;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.apdc.assignment.util.data.WorkSheetData;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class DatastoreWorkSheet {

    private static final Datastore ds = DatastoreOptions.getDefaultInstance().getService();
    private static final String EMPTY_STRING = "NOT DEFINED";

    public static Response createWorkSheet(WorkSheetData data, String role, String username) {
        if (!role.equals("BACKOFFICE") && !role.equals("ADMIN"))
            return Response.status(Response.Status.FORBIDDEN).entity("Permissão negada.").build();

        if (!data.isValidForBackofficeCreate())
            return Response.status(Response.Status.BAD_REQUEST).entity("Campos obrigatórios em falta.").build();

        Key workKey = ds.newKeyFactory().setKind("WorkSheet").newKey(data.reference);
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

        ds.put(builder.build());
        return Response.ok("Folha de obra criada com sucesso.").build();
    }

    public static Response adjudicateWorkSheet(WorkSheetData data, String role, String username) {
        if (!role.equals("BACKOFFICE") && !role.equals("ADMIN"))
            return Response.status(Response.Status.FORBIDDEN).entity("Permissão negada.").build();

        if (!data.isValidForAdjudication())
            return Response.status(Response.Status.BAD_REQUEST).entity("Campos obrigatórios para adjudicação em falta.").build();

        Key workKey = ds.newKeyFactory().setKind("WorkSheet").newKey(data.reference);
        Entity ws = ds.get(workKey);

        if (ws == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Folha de obra não encontrada.").build();

        if (ws.getString("adjudicationStatus").equals("ADJUDICADO"))
            return Response.status(Response.Status.CONFLICT).entity("Folha de obra já adjudicada.").build();{
        }

        String info = data.reference + data.description + data.targetType + data.adjudicationStatus +
                data.adjudicationDate + data.startDate + data.endDate + data.partnerAccount +
                data.companyName + data.companyNIF + data.workState + data.observations;
        String newToken = generateWorkToken(info);

        Entity updated = Entity.newBuilder(ws)
            .set("adjudicationStatus", "ADJUDICADO")
            .set("adjudicationDate", data.adjudicationDate)
            .set("startDate", data.startDate)
            .set("endDate", data.endDate)
            .set("partnerAccount", data.partnerAccount)
            .set("companyName", data.companyName)
            .set("companyNIF", data.companyNIF)
            .set("worksheet_token", newToken)
            .build();

        ds.put(updated);
        return Response.ok("Folha de obra adjudicada com sucesso.").build();
    }

    public static Response updateProgress(WorkSheetData data, String role, String username) {
        if (!role.equals("PARTNER") && !role.equals("ADMIN"))
            return Response.status(Response.Status.FORBIDDEN).entity("Permissão negada.").build();

        if (!data.isValidForPartnerUpdate())
            return Response.status(Response.Status.BAD_REQUEST).entity("Campos obrigatórios para progress update em falta.").build();

        Key workKey = ds.newKeyFactory().setKind("WorkSheet").newKey(data.reference);
        Entity ws = ds.get(workKey);

        if (ws == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Folha de obra não encontrada.").build();

        if (!username.equals(ws.getString("partnerAccount")) && !role.equals("ADMIN"))
            return Response.status(Response.Status.FORBIDDEN).entity("Não tens permissão para editar esta obra.").build();

        Entity updated = Entity.newBuilder(ws)
            .set("workState", data.workState)
            .set("observations", data.observations != null ? data.observations : EMPTY_STRING)
            .build();

        //String com todos os campos de WorkSheetData
        String info = data.reference + data.description + data.targetType + data.adjudicationStatus +
                data.adjudicationDate + data.startDate + data.endDate + data.partnerAccount +
                data.companyName + data.companyNIF + data.workState + data.observations;
        String newToken = generateWorkToken(info);

        if (newToken.equals(ws.getString("worksheet_token"))) {
            return Response.status(Response.Status.CONFLICT).entity("Não foram feitas alterações.").build();
        } else {
            updated = Entity.newBuilder(updated)
                .set("worksheet_token", newToken)
                .build();
        }

        ds.put(updated);
        return Response.ok("Progresso da obra atualizado com sucesso.").build();
    }

    private static String generateWorkToken(WorkSheetData data) {
        try {
            String input = (data.reference + data.description + data.targetType + data.adjudicationStatus).trim();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private static String generateWorkToken(String data) {
        try {
            String input = data.trim();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "";
        }
    }
}
