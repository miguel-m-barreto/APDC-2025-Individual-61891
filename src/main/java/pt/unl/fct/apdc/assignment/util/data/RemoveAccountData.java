package pt.unl.fct.apdc.assignment.util.data;

public class RemoveAccountData {
    public String requesterID;   // tokenID, username ou email
    public String targetUser;    // username ou email

    public RemoveAccountData() {}

    public RemoveAccountData(String requesterID, String targetUser) {
        this.requesterID = requesterID;
        this.targetUser = targetUser;
    }
}
