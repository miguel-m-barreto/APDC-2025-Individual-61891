package pt.unl.fct.apdc.assignment.util;

import java.util.UUID;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60*2;
	
	public String username;
	public String tokenID;
	public long creationData;
	public long expirationData;
	public String role;
	public String verificationCode;

	private static final String SECRET_KEY = "SuperSecretKey123"; 
	
	public AuthToken() {

	}
	
	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + EXPIRATION_TIME;
		this.verificationCode = generateVerifier();
	}

	private String generateVerifier() {
		try {
			String data = username + tokenID + creationData + expirationData + role;
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
