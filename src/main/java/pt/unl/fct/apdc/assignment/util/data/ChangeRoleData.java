package pt.unl.fct.apdc.assignment.util.data;

public class ChangeRoleData {
    public String requesterID;
    public String username; //username of the requester
    public String targetUser;
    public String newRole;

    public ChangeRoleData() {}

    public ChangeRoleData(String requesterID, String targetUser, String newRole) {
        this.requesterID = requesterID; //tokenID username or email
        this.targetUser = targetUser; //username, email
        this.newRole = newRole;
    }
}
