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
}
