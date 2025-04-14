package pt.unl.fct.apdc.assignment.util;

public class StringUtil {

    private static final String PUBLIC = "public";
	private static final String PRIVATE = "private";
    private static final String EMPTY_STRING = "NOT DEFINED";

    /**
     * Verifica se o campo não é nulo nem vazio (em branco).
     */
    public static final boolean isNonEmpty(String input) {
        return input != null && !input.isBlank();
    }

    /**
     * Normaliza uma string para lowercase e remove espaços do fim e inicio.
     */
    public static final String normalizeStringLowerCase(String input) {
        if (!isNonEmpty(input)) return null;
        return input.trim().toLowerCase();
    }

    public static final String normalizeStringUpperCase(String email) {
        if (email == null || email.isEmpty()) return null;
        return email.trim().toUpperCase();
    }

    public static final String normalizeString(String input) {
        if (input == null || input.isEmpty()) return null;
        return input.trim();
    }

    public static final String normalizeEmail(String email) {
        return normalizeStringLowerCase(email);
    }

    public static final String normalizeProfileType(String profileType) {
		if (profileType == null || profileType.isEmpty()) return null;
	
		profileType = normalizeStringLowerCase(profileType);
	
		switch (profileType.toUpperCase()) {
			case "PUBLIC":
			case "PUBLICO":
			case "PÚBLICO":
				return PUBLIC;
            case "PRIVATE":
            case "PRIVADO":
            case "PRIVADA":
                return PRIVATE;
			default:
                return EMPTY_STRING;
		}
	}	

    public static final String normalizeWorksheetStatus(String status) {
        if (status == null || status.isEmpty()) return EMPTY_STRING;    
        status = normalizeStringUpperCase(status);
        switch (status) {
            case "NAO_INICIADO":
            case "NÃO_INICIADO":
            case "NAO INICIADO":
            case "NÃO INICIADO":
            case "NAO_INICIADA":
            case "NÃO INICIADA":
            case "NAO INICIADA":
            case "NÃO_INICIADA":
                return "NAO_INICIADO";
            case "ADJUDICADO":
            case "ADJUDICADA":
                return "ADJUDICADO";
            case "NAO_ADJUDICADO":
            case "NÃO_ADJUDICADO":
            case "NAO_ADJUDICADA":
            case "NÃO_ADJUDICADA":
            case "NAO ADJUDICADO":
            case "NÃO ADJUDICADO":
            case "NAO ADJUDICADA":
            case "NÃO ADJUDICADA":
                return "NAO_ADJUDICADO";
            case "EM_CURSO":
            case "EM CURSO":
                return "EM_CURSO";
            case "CONCLUIDO":
            case "CONCLUÍDO":
            case "CONCLUIDA":
            case "CONCLUÍDA":
                return "CONCLUIDO";
            default:
                return EMPTY_STRING;
        }
    }


}
