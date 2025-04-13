package pt.unl.fct.apdc.assignment.util;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AuthToken {

	// Token expiration time in milliseconds (2 hours)
	// public static final long EXPIRATION_TIME = 1000*60*60*2;
	// Token expiration time in milliseconds (1 minute)
	// public static final long EXPIRATION_TIME = 1000*60*1;
	// Token expiration time in milliseconds (30 seconds)
	public static final long EXPIRATION_TIME = 1000*30;

	private static final String SECRET_KEY = "SuperSecretKey123"; 

	public String username;
	public String role;
	public long validFrom;
	public long validTo;
	public String verificationCode;
	public String tokenID;
	
	public AuthToken() {

	}
	
	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.validFrom = System.currentTimeMillis();
		this.validTo = this.validFrom + EXPIRATION_TIME;
		this.tokenID = UUID.randomUUID().toString();
		this.verificationCode = getVerifier(tokenID);
	}

	public static String getVerifier(String token) {
		try {
			String data = token;
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
			mac.init(keySpec);
			byte[] hmacBytes = mac.doFinal(data.getBytes());
			return Base64.getEncoder().encodeToString(hmacBytes);
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate token verifier", e);
		}
	}
}
