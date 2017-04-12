import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting Client connection...");
		try {
			// Connect to server
			Socket clientSocket = new Socket(InetAddress.getByName("localhost"), 16000);
			
			// Create input and output communication streams
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out =  new PrintWriter(clientSocket.getOutputStream(), true);
			ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
			
			// Generate Private Key and Public Key Pair with Diffie Hellman
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
			keyGen.initialize(512);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey secretkey = pair.getPrivate();
			PublicKey publickey = pair.getPublic();
			
			//KeyAgreement Setup
			KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
			keyAgree.init(secretkey);
			//keyAgree.doPhase(secretkey, false);
			
			// Send Public Key
			os.writeObject(publickey);

			// Receive Client's Public Key
			PublicKey received_publickey = (PublicKey) is.readObject();
			
			// Generate Secret Key
			keyAgree.doPhase(received_publickey, true);
			byte[] shared_secret = keyAgree.generateSecret();

			String test = "hello";
			byte[] tester = test.getBytes();

			int[] ret = new int[tester.length];
			for (int i = 0; i < tester.length; i++) {
				ret[i] = tester[i];
			}

			// Convert received message to int array
			int[] secret_key = new int[shared_secret.length];
			for (int i = 0; i < shared_secret.length; i++) {
				secret_key[i] = shared_secret[i];
			}

			MyEncrypt encrypt = new MyEncrypt(secret_key, ret);
			encrypt.encryption();
			System.out.println("Here's the messsage I ENCRYPTED: " + Arrays.toString(ret));
			os.writeObject(ret);
		}
		catch (Exception e){
			System.out.println("Error");
		}
	}
}
