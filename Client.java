import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting Client connection...");
		try {
			Socket clientSocket = new Socket(InetAddress.getByName("localhost"), 16000);
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out =  new PrintWriter(clientSocket.getOutputStream(), true);
			
			// Generate Private Key
//			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//			keyGen.init(256);
//			SecretKey secretkey = keyGen.generateKey();
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
			keyGen.initialize(128);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey secretkey = pair.getPrivate();
			PublicKey publickey = pair.getPublic();
			
			String encodedKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
			System.out.println("Here's my privatekey: " + encodedKey);
			
			//Setup KeyAgreement
			KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
			keyAgree.init(secretkey);
			keyAgree.doPhase(secretkey, false);
			
			// Encode key
			encodedKey = Base64.getEncoder().encodeToString(publickey.getEncoded());
			
			System.out.println("Here's what I'm about to send" + encodedKey);
			// Send public key
			out.println(encodedKey);

			String message = input.readLine();
			System.out.println("Here's what I got" + message);
			
			byte[] decodedkey = Base64.getDecoder().decode(message);
			
			Key received_public_key = new SecretKeySpec(decodedkey, 0, decodedkey.length, "AES");
			keyAgree.doPhase(received_public_key, true);
			
			// Generate Secret Key
			SecretKey shared_secret = keyAgree.generateSecret("AES");
			encodedKey = Base64.getEncoder().encodeToString(shared_secret.getEncoded());
			System.out.println("HERE IS MY SECRET KEY!!!" + encodedKey);
		}
		catch (Exception e){
			System.out.println("Error");
		}
	}
}
