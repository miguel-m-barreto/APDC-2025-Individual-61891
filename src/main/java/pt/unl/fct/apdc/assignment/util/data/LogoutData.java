package pt.unl.fct.apdc.assignment.util.data;

public class LogoutData {
    public String requesterID;
    public String tokenString;

    public LogoutData() {}

    public LogoutData(String requesterID, String tokenString) {
        this.requesterID = requesterID;
        this.tokenString = tokenString;
    }

    public boolean isValid() {
        return requesterID != null && !requesterID.isBlank() &&
                tokenString != null && !tokenString.isBlank();
    }
}
