package pt.unl.fct.apdc.assignment.util.data;

public class ListUsersData {
    public String requesterID;
    public String token; // Token de autenticação do utilizador que está a fazer a alteração

    public ListUsersData() {}

    public ListUsersData(String requesterID, String token) {
        this.requesterID = requesterID;
        this.token = token; // Token de autenticação do utilizador que está a fazer a alteração
    }

    public boolean validAttributes() {
        return requesterID != null && token != null;
    }
}
