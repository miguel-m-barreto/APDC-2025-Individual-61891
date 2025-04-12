package pt.unl.fct.apdc.assignment.util;

public class StringUtil {

    private static final String PUBLIC = "público";
	private static final String PRIVATE = "privado";

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
        return email.trim().toLowerCase();
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
	
		switch (profileType) {
			case "public":
			case "publico":
			case "público":
				return PUBLIC;
			default:
                return PRIVATE;
		}
	}	

}
