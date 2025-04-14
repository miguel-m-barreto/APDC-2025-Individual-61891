package pt.unl.fct.apdc.assignment.util.data;

public class WorkSheetData {
    public String requesterID;

    // Criação
    public String reference;
    public String description;
    public String targetType; // "Propriedade Pública" ou "Propriedade Privada"
    public String adjudicationStatus; // "ADJUDICADO" ou "NÃO ADJUDICADO"

    // Adjudicação
    public String adjudicationDate;
    public String startDate;
    public String endDate;
    public String partnerAccount;
    public String companyName;
    public String companyNIF;

    // Progresso da obra (Partner)
    public String workState; // "NÃO INICIADO", "EM CURSO", "CONCLUÍDO"
    public String observations;
    public String token;

    // Regex para validar datas
    private static final String DATE_REGEX =
            "^\\d{2}[-/]((0[1-9])|(1[0-2])|(?i)(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))[-/]\\d{4}$";
    private static final String ISO_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";

    /** Validação usada na criação por BACKOFFICE */
    public boolean isValidForBackofficeCreate() {
        if (isBlank(reference) || isBlank(description) || isBlank(targetType) || isBlank(adjudicationStatus)) {
            return false;
        }

        if (adjudicationStatus.equalsIgnoreCase("ADJUDICADO")) {
            return isValidForAdjudication(); // reutiliza lógica
        }

        return true;
    }

    /** Validação para adjudicação */
    public boolean isValidForAdjudication() {
        return isValidDate(adjudicationDate) &&
               isValidDate(startDate) &&
               isValidDate(endDate) &&
               !isBlank(partnerAccount) &&
               !isBlank(companyName) &&
               companyNIF != null && companyNIF.matches("\\d{9}");
    }

    /** Validação para progress update por PARTNER */
    public boolean isValidForPartnerUpdate() {
        return !isBlank(reference) &&
               !isBlank(workState) &&
               isValidWorkState(workState);
    }

    private boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }

    private boolean isValidDate(String date) {
        return date != null && (date.matches(DATE_REGEX) || date.matches(ISO_DATE_REGEX));
    }

    private boolean isValidWorkState(String state) {
        String s = state.toUpperCase();
        return s.equals("NÃO INICIADO") || s.equals("EM CURSO") || s.equals("CONCLUÍDO");
    }
}
