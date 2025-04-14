package pt.unl.fct.apdc.assignment.util.data;

public class ChangePasswordData {
    public String requesterID;       // Token, username ou email
    public String oldPassword;
    public String newPassword;
    public String confirmation;
    public String token;            // Token de autenticação do utilizador que está a fazer a alteração

    public ChangePasswordData() {}

    public ChangePasswordData(String requesterID, String oldPassword, String newPassword, String confirmation, String token) {
        this.requesterID = requesterID;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmation = confirmation;
        this.token = token; // Token de autenticação do utilizador que está a fazer a alteração
    }

    public boolean validAttributes() {
        return requesterID != null && oldPassword != null && newPassword != null && token != null &&
               newPassword.equals(confirmation) &&
               newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,32}$");
    }
}
