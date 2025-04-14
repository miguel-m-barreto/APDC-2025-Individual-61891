package pt.unl.fct.apdc.assignment.util.data;

public class RemoveAccountData {
    public String requesterID;
    public String targetUser;
    public String token; // Token de autenticação do utilizador que está a fazer a alteração

    public RemoveAccountData() {}

    public RemoveAccountData(String requesterID, String targetUser) {
        this.requesterID = requesterID;
        this.targetUser = targetUser;
    }
}
