package pt.unl.fct.apdc.assignment.util;

public class Permission {

    public static boolean canChangeRole(String requesterRole, String currentRole, String newRole) {
        requesterRole = requesterRole.toUpperCase();
        currentRole = currentRole.toUpperCase();
        newRole = newRole.toUpperCase();

        switch (requesterRole) {
            case "ADMIN":
                return true;

            case "BACKOFFICE":
                return (isEndUserOrPartner(currentRole) && isEndUserOrPartner(newRole));

            case "ENDUSER":
            default:
                return false;
        }
    }

    private static boolean isEndUserOrPartner(String role) {
        return role.equals("ENDUSER") || role.equals("PARTNER");
    }

    public static boolean canChangeState(String requesterRole, String currentState, String newState) {
        requesterRole = requesterRole.toUpperCase();
        currentState = currentState.toUpperCase();
        newState = newState.toUpperCase();
    
        if (requesterRole.equals("ADMIN")) return true;
    
        if (requesterRole.equals("BACKOFFICE")) {
            // Só pode alternar entre ATIVADA e DESATIVADA
            return  newState.equals("DESATIVADA") || newState.equals("ATIVADA");
        }
    
        return false;
    }

    public static boolean canRemoveAccount(String requesterRole, String targetRole) {
        if (requesterRole.equals("ADMIN")) {
            return true;            
        }
        return (requesterRole.equals("BACKOFFICE") && ((targetRole.equals("ENDUSER") || targetRole.equals("PARTNER"))));
    }    

    public static boolean canChangeAttributes(String requesterRole, String requesterUsername, String targetUsername,
                                              String targetRole, boolean targetIsActivated,
                                              boolean isTryingToChangeControlFields, boolean isTryingToChangeEmailOrUsername) {

        requesterRole = requesterRole.toUpperCase();
        targetRole = targetRole.toUpperCase();

        // ADMIN pode tudo
        if (requesterRole.equals("ADMIN")) {
            return true;
        }

        // BACKOFFICE pode mudar atributos de ENDUSER ou PARTNER (se ativado), exceto username/email
        if (requesterRole.equals("BACKOFFICE")) {
            if (!targetIsActivated) return false;
            if ((targetRole.equals("ENDUSER") || targetRole.equals("PARTNER"))) return true;
            return !isTryingToChangeEmailOrUsername;
        }

        // ENDUSER pode modificar só a própria conta
        if (requesterRole.equals("ENDUSER") /*|| requesterRole.equals("PARTNER")*/) {
            if (!requesterUsername.equals(targetUsername)) return false;
            // Não pode alterar username/email/nome nem os de controlo (role/state)
            return !isTryingToChangeControlFields && !isTryingToChangeEmailOrUsername;
        }

        return false;
    }
    
}
