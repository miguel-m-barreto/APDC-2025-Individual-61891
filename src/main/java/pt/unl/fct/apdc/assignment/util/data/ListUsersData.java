package pt.unl.fct.apdc.assignment.util.data;

public class ListUsersData {
    public String requesterID;
    public String token; // Token de autenticação do utilizador que está a fazer a alteração

    public ListUsersData() {}

    public ListUsersData(String requesterID) {
        this.requesterID = requesterID;
    }
}
