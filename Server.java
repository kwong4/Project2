import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.util.Base64;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Server implements Runnable {
	Socket csocket;
	Server(Socket csocket) {
		this.csocket = csocket;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Starting Server connection...");
		
		try {
			ServerSocket serverSocket = new ServerSocket(16000);
			while (true) {
				Socket sock = serverSocket.accept();
				System.out.println("New Client Accepted!!");
				new Thread(new Server(sock)).start();
			}
		} catch (Exception e) {
			System.out.println("Error!");
		}
	}

	public void run() {
		try {
			PrintWriter out =  new PrintWriter(csocket.getOutputStream(), true);
			BufferedReader input = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
			
			// Generate Private Key
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			SecretKey secretkey = keyGen.generateKey();
			
			System.out.println("Here's my privatekey: " + secretkey);
			
			//Setup KeyAgreement
			KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
			keyAgree.init(secretkey);
			Key publickey = keyAgree.doPhase(secretkey, false);
			
			// Encode key
			String encodedKey = Base64.getEncoder().encodeToString(publickey.getEncoded());
			
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
			System.out.println("HERE IS MY SECRET KEY!!!" + shared_secret);
			
			csocket.close();
		} catch(Exception e) {
			System.out.println(e);
		}
		finally {
		}
	}
}
