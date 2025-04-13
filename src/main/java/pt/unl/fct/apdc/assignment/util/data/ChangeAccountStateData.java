package pt.unl.fct.apdc.assignment.util.data;

public class ChangeAccountStateData {
    public String requesterID;     // tokenID, username ou email
    public String targetUser;      // username ou email
    public String newState;        // "ATIVADA", "DESATIVADA", "SUSPENSA"

    public ChangeAccountStateData() {}

    public ChangeAccountStateData(String requesterID, String targetUser, String newState) {
        this.requesterID = requesterID;
        this.targetUser = targetUser;
        this.newState = newState;
    }
}
