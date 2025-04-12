package pt.unl.fct.apdc.assignment.util.data;

import static pt.unl.fct.apdc.assignment.util.StringUtil.isNonEmpty;

public class RegisterData {
	
	public String username;
	public String password;
	public String confirmation;
	public String email;
	public String name;
	public String phone;
	public String profileType; // "público" ou "privado"

	// Atributos opcionais
	public String cc;
	public String nif;	// Nif do utilizador
	public String employer;
	public String job;
	public String address;
	public String employerNIF;	// NIF da entidade empregadora
	public String accountState; // "ATIVADA" ou "DESATIVADA" ou "SUSPENSA"

	public RegisterData() {
		
	}
	
	public RegisterData(String username, String password, String confirmation, String email, 
						String name, String phone, String profileType, String cc, String nif,
						String employer, String job, String address, String employerNIF, String photo) {
		// Mandatory data
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.name = name;
		this.phone = phone;
		// Non case sensitive mandatory parameters
		this.email = email; // Lowercase email
		this.profileType = profileType; // Lowercase profileType with normalizeProfileType method
		
		
		// OPTIONAL DATA
		this.cc = cc;
		this.nif = nif;
		this.employer = employer;
		this.job = job;
		this.address = address;
		this.employerNIF = employerNIF;
	}

	// Método para validar os dados de registo
	public boolean validRegistration() {
		 	
		return isNonEmpty(username) &&
				isNonEmpty(password) &&
				isNonEmpty(email) &&
				isNonEmpty(name) &&
				isNonEmpty(phone) &&
				isNonEmpty(profileType) &&

				//	A pass tem de conter uma combinação de caracteres alfabéticos (maiúsculas e minúsculas), 
				//	numéricos e sinais de pontuação 
				//	tem de ter pelo menos 8 caracteres e no máximo 32
				password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,32}$") &&			   
				password.equals(confirmation) &&

				name.matches("^\\S+(?:\\s+\\S+)*$") &&	// 1 ou mais palavras

				// Regex para validar o numero de telefone internacional (recomendado pelo ITU-T E.164) ou Numero PT (Nacional, sem identificador)
				(phone.matches("^\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|4[987654310]|3[9643210]|2[70]|7|1)\\d{1,14}$") || 
				phone.matches("9[1236][0-9]{7}")) &&

				// Condições para validar o email
				email.matches("^[A-Za-z0-9+_.-]{1,30}@[A-Za-z0-9.-]{1,30}\\.[A-Za-z]{2,6}$") &&	 // Formato de email válido

				// Condições para validar o profileType
				// O profileType deve ser "público" ou "privado"
				profileType.matches("(?i)public|publico|público|privado|private") &&
				
				// Codiçoes para validar os campos opcionais
	
				(cc == null || cc.matches("[0-9]{8}")) &&
				(nif == null || nif.matches("[0-9]{9}")) &&
				(employerNIF == null || employerNIF.matches("[0-9]{9}")) &&
				(job == null || job.length() <= 32) &&
				(address == null || address.length() <= 100) &&
				(employer == null || employer.length() <= 32);
	}
}
