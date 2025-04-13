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

    // Campos de controlo (apenas para ADMIN e BACKOFFICE)
    public String role;
    public String state;

    // Atributos de ADMIN (apenas para ADMIN)
    public String username; 
    public String email;    
    /*public String password;*/
    public String confirmation; // Confirmação da password (para evitar erros de digitação)

    public ChangeAttributesData() {}

    public ChangeAttributesData(String requesterID, String targetUsername, String name, String phone,
                                String address, String job, String employer, String nif, String cc,
                                String profile, String employer_nif, String photoURL, String role,
                                String state, String username, String email, /*String password,*/ String confirmation) {
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
        // Admin attributes
        this.username = username;
        this.email = email;
        /*this.password = password;*/
        this.confirmation = confirmation;
        
    }

    // Método para validar os dados de registo
	public boolean validAttributes() {
		 	
		return (requesterID != null) &&

				//	A pass tem de conter uma combinação de caracteres alfabéticos (maiúsculas e minúsculas), 
				//	numéricos e sinais de pontuação 
				//	tem de ter pelo menos 8 caracteres e no máximo 32
                /*password == null || (
                isNonEmpty(password) &&
                password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,32}$") &&			   
				password.equals(confirmation)) &&*/

                (name == null ||
				name.matches("^\\S+(?:\\s+\\S+)*$")) &&	// 1 ou mais palavras

				// Regex para validar o numero de telefone internacional (recomendado pelo ITU-T E.164) ou Numero PT (Nacional, sem identificador)
				(phone == null ||
                (phone.matches("^\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|4[987654310]|3[9643210]|2[70]|7|1)\\d{1,14}$") || 
				phone.matches("9[1236][0-9]{7}"))) &&

				// Condições para validar o email
                (email == null ||
				email.matches("^[A-Za-z0-9+_.-]{1,30}@[A-Za-z0-9.-]{1,30}\\.[A-Za-z]{2,6}$")) &&	 // Formato de email válido
				// Condições para validar o profileType
				// O profileType deve ser "público" ou "privado"
                (
                profile == null ||
				profile.matches("(?i)public|publico|público|privado|private")) &&
				
				// Codiçoes para validar os campos opcionais
	
				(cc == null || cc.matches("[0-9]{8}")) &&
				(nif == null || nif.matches("[0-9]{9}")) &&
				(employer_nif == null || employer_nif.matches("[0-9]{9}")) &&
				(job == null || job.length() <= 32) &&
				(address == null || address.length() <= 100) &&
				(employer == null || employer.length() <= 32);

    }
}
