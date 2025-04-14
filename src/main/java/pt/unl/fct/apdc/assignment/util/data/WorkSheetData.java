package pt.unl.fct.apdc.assignment.util.data;

public class WorkSheetData {
    public String requesterID;

    // Atributos obrigatorios na criaçao
    public String reference;
    public String description;
    public String targetType; // "Propriedade Publica" ou "Propriedade Privada"
    public String adjudicationStatus; // "ADJUDICADO" ou "NAO ADJUDICADO"

    // Atributos so preenchidos se ADJUDICADO
    public String adjudicationDate;
    public String startDate;
    public String endDate;
    public String partnerAccount;
    public String companyName;
    public String companyNIF;

    // Atributos que podem ser modificados por PARTNER
    public String workState; // "NAO INICIADO", "EM CURSO", "CONCLUÍDO"
    public String observations;

    private static final String DATE_REGEX =
            "^\\d{2}[-/]((0[1-9])|(1[0-2])|(?i)(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))[-/]\\d{4}$";
    private static final String ISO_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$"; // opcional

    public boolean isValidForBackofficeCreate() {
        if (isBlank(reference) || isBlank(description) || isBlank(targetType) || isBlank(adjudicationStatus)) {
            return false;
        }

        if (adjudicationStatus.equalsIgnoreCase("ADJUDICADO")) {
            return isValidDate(adjudicationDate) &&
                   isValidDate(startDate) &&
                   isValidDate(endDate) &&
                   !isBlank(partnerAccount) &&
                   !isBlank(companyName) &&
                   companyNIF != null && companyNIF.matches("\\d{9}");
        }

        return true;
    }

    public boolean isValidForPartnerUpdate() {
        return !isBlank(reference) && !isBlank(workState);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidDate(String date) {
        return date != null && (date.matches(DATE_REGEX) || date.matches(ISO_DATE_REGEX));
    }
}
