package pt.unl.fct.apdc.assignment.util.data;

public class ChangeRoleData {
    public String requesterID; // tokenID, username ou email
    public String targetUser;  // username ou email
    public String newRole;
    public String token;      // Token de autenticação do utilizador que está a fazer a alteração

    public ChangeRoleData() {}

    public ChangeRoleData(String requesterID, String targetUser, String newRole) {
        this.requesterID = requesterID;
        this.targetUser = targetUser;
        this.newRole = newRole;
    }
}
