package pt.unl.fct.apdc.assignment.util.data;

public class WorkSheetData {
    public String requesterID;

    // Atributos obrigatórios na criação
    public String reference;
    public String description;
    public String targetType; // "Propriedade Pública" ou "Propriedade Privada"
    public String adjudicationStatus; // "ADJUDICADO" ou "NÃO ADJUDICADO"

    // Atributos só preenchidos se ADJUDICADO
    public String adjudicationDate;
    public String startDate;
    public String endDate;
    public String partnerAccount;
    public String companyName;
    public String companyNIF;

    // Atributos que podem ser modificados por PARTNER
    public String workState; // "NÃO INICIADO", "EM CURSO", "CONCLUÍDO"
    public String observations;

    public boolean isValidForBackofficeCreate() {
        return reference != null && description != null && targetType != null &&
               (adjudicationStatus != null && (adjudicationStatus.equalsIgnoreCase("ADJUDICADO") ||
                                               adjudicationStatus.equalsIgnoreCase("NÃO ADJUDICADO")));
    }

    public boolean isValidForPartnerUpdate() {
        return reference != null && workState != null;
    }
}
