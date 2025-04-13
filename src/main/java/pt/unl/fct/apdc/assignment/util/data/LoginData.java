package pt.unl.fct.apdc.assignment.util.data;

public class LoginData {
	
	public String requesterID; // username or email
	public String password;
	
	/* Construtor vazio 
	 * É necessário para o Gson conseguir fazer o parse do JSON para a classe LoginData
	 * ---
	 * Slide F3
	 * quando se recebe um JSON do cliente que tem de ser convertido para uma instância java.
	 * Sem este construtor, isso não seria possível de fazer
	 * ---
	 * Caso contrário, o Gson não consegue instanciar a classe LoginData
	 * e dá erro de NullPointerException
	 */
	public LoginData() {
		
	}
	
	public LoginData(String requesterID, String password) {
		this.requesterID = requesterID;
		this.password = password;
	}
	
}
