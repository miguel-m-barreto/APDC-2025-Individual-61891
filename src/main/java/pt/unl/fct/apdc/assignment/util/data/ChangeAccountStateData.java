package pt.unl.fct.apdc.assignment.util.data;

public class ChangeAccountStateData {
    public String requesterID;     // tokenID, username ou email
    public String targetUser;      // username ou email
    public String newState;        // "ATIVADA", "DESATIVADA", "SUSPENSA"
    public String token;

    public ChangeAccountStateData() {}

    public ChangeAccountStateData(String requesterID, String targetUser, String newState, String token) {
        this.requesterID = requesterID;
        this.targetUser = targetUser;
        this.newState = newState;
        this.token = token;
    }

    public boolean validAttributes() {
        return requesterID != null && newState != null && token != null;
    }
}
