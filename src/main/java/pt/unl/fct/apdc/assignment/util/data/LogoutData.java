package pt.unl.fct.apdc.assignment.util.data;

public class LogoutData {
    public String requesterID;
    public String token;

    public LogoutData() {}

    public LogoutData(String requesterID, String token) {
        this.requesterID = requesterID;
        this.token = token;
    }

    public boolean isValid() {
        return requesterID != null && !requesterID.isBlank() &&
                token != null && !token.isBlank();
    }
}
