package pt.unl.fct.apdc.assignment.util.data;

public class RemoveAccountData {
    public String requesterID;
    public String targetUser;
    public String token; // Token de autenticação do utilizador que está a fazer a alteração

    public RemoveAccountData() {}

    public RemoveAccountData(String requesterID, String targetUser, String token) {
        this.requesterID = requesterID;
        this.targetUser = targetUser;
        this.token = token; // Token de autenticação do utilizador que está a fazer a alteração
    }

    public boolean validAttributes() {
        return requesterID != null && targetUser != null && token != null;
    }
}
