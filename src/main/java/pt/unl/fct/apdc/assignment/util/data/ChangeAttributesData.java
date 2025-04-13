package pt.unl.fct.apdc.assignment.util.data;

public class ChangeAttributesData {
    public String requesterID;    // TokenID, username ou email de quem está a pedir
    public String targetUsername; // Utilizador que será alterado (pode ser o próprio)

    // Atributos que podem ser alterados
    public String name;
    public String phone;
    public String address;
    public String job;
    public String employer;
    public String nif;
    public String cc;
    public String profile;
    public String employer_nif;
    public String photoURL;

    // Campos de controlo (apenas para ADMIN)
    public String role;
    public String state;

    // Não podem ser alterados (mas colocados aqui para validação)
    public String username; // usado apenas para bloquear alterações indevidas
    public String email;    // idem

    public ChangeAttributesData() {}

    public ChangeAttributesData(String requesterID, String targetUsername, String name, String phone,
                                String address, String job, String employer, String nif, String cc,
                                String profile, String employer_nif, String photoURL, String role,
                                String state, String username, String email) {
        this.requesterID = requesterID;
        this.targetUsername = targetUsername;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.job = job;
        this.employer = employer;
        this.nif = nif;
        this.cc = cc;
        this.profile = profile;
        this.employer_nif = employer_nif;
        this.photoURL = photoURL;
        this.role = role;
        this.state = state;
        this.username = username;
        this.email = email;
    }
}
